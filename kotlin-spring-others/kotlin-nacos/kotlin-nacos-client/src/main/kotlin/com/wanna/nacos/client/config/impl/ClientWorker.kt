package com.wanna.nacos.client.config.impl

import com.wanna.framework.web.client.RestTemplate
import com.wanna.nacos.api.PropertyKeyConst
import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.api.config.listener.Listener
import com.wanna.nacos.client.config.common.ConfigConstants
import com.wanna.nacos.client.config.filter.impl.ConfigResponse
import com.wanna.nacos.client.config.http.HttpAgent
import com.wanna.nacos.client.config.http.ServerHttpAgent
import com.wanna.nacos.client.utils.ParamUtils
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * NacosConfigClient的长轮询任务的Worker, 负责将添加进来的所有的[CacheData]
 * 去进行不断地轮询请求ConfigServer, 如果ConfigServer的配置文件发生变更的话,
 * 那么需要回调该[CacheData]内部维护的所有的Listener
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 *
 * @param properties 对于ConfigClient的一些配置信息(比如长轮询的时间)
 */
open class ClientWorker(private val properties: Properties) : Closeable {

    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(ClientWorker::class.java)

    /**
     * 初始化HttpAgent
     */
    private val agent: HttpAgent = ServerHttpAgent(properties)

    /**
     * ConfigClient的客户端长轮询的timeout(默认为30s, 最低也为30s, 可以通过Properties当中去配置"configLongPollTimeout"使用高于30s的超时时间)
     */
    private var timeout: Long = 0L

    /**
     * 维护所有的配置文件数据以及监听该配置文件的所有的Listener列表
     */
    private var cacheMap = ConcurrentHashMap<String, CacheData>()

    /**
     * 当前的LongPolling任务的数量
     */
    private var currentLongingTaskCount = 0

    /**
     * LongPolling ScheduledExecutorService, 提供对于长轮询任务的执行
     */
    private val longPollingExecutor =
        Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors()
        ) {
            val thread = Thread(it)
            thread.name = "com.wanna.nacos.client.Worker.LongPolling"
            thread.isDaemon = true
            thread
        }

    /**
     * 检查LongPollingTask数量是否足够的线程池, 每个10s去检查一下是否需要去对LongPollingTask去进行扩容;
     * 默认情况下, 当CacheData的数量超过3000时就需要去进行扩容
     */
    private val checkConfigInfoExecutor = Executors.newScheduledThreadPool(1) {
        val thread = Thread(it)
        thread.name = "com.wanna.nacos.client.Worker.CheckConfigInfo"
        thread.isDaemon = true
        thread
    }

    init {

        // 利用Properties去完成初始化...
        init(properties)

        // 在初始化时, 就添加一个10s的定时任务去执行checkConfigInfo, 检查是否需要扩容LongPollingTask
        checkConfigInfoExecutor.scheduleWithFixedDelay({ checkConfigInfo() }, 1L, 10L, TimeUnit.MILLISECONDS)
    }

    private fun init(properties: Properties) {
        // 初始化长轮询的超时时间, 如果配置了一个低于30s的, 那么将会采用30s; 可以配置比30s更高
        this.timeout = maxOf(
            properties[PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT]?.toString()?.toLongOrNull()
                ?: Constants.CONFIG_LONG_POLL_TIMEOUT, Constants.MIN_CONFIG_LONG_POLL_TIMEOUT
        )
    }

    /**
     * 为某个namespace的ClientWorker去添加Listener
     *
     * @param dataId dataId
     * @param group group
     * @param listeners 需要添加的监听器列表
     */
    open fun addTenantListeners(dataId: String, group: String, listeners: List<Listener>) {
        val cacheData = addCacheDataIfAbsent(dataId, group, agent.getTenant())
        listeners.forEach(cacheData::addListener)
    }

    /**
     * 从某个namespace的ClientWorker当中移除一个监听dataId和group对应的配置文件的Listener
     *
     * @param dataId dataId
     * @param group group
     * @param listener 要去进行移除的Listener
     */
    open fun removeTenantListener(dataId: String, group: String, listener: Listener) {
        getCacheData(dataId, group, "")?.removeListener(listener)
    }

    /**
     * 根据dataId&group&tenant去获取到对应的CacheData
     *
     * @param dataId dataId
     * @param group group
     * @param tenant tenant
     * @return 获取到的CacheData
     */
    open fun getCacheData(dataId: String, group: String, tenant: String): CacheData? {
        return cacheMap[GroupKey.getKeyTenant(dataId, group, tenant)]
    }

    /**
     * 如果必要的话, 往cacheMap当中去添加一个CacheData
     *
     * @param dataId dataId
     * @param group group
     * @param tenant tenant(namespace)
     */
    open fun addCacheDataIfAbsent(dataId: String, group: String, tenant: String): CacheData {
        // 根据dataId&group&tenant去生成Key
        val cacheKey = GroupKey.getKeyTenant(dataId, group, tenant)
        var cacheData = cacheMap[cacheKey]
        if (cacheData != null) {
            return cacheData
        }
        cacheData = CacheData(dataId, group, tenant)
        cacheMap[cacheKey] = cacheData
        return cacheData
    }

    /**
     * 关闭当前的ClientWorker, 需要关闭检查ConfigInfo的线程池, 同时去关闭用于长轮询拉取ConfigServer配置文件的线程池
     */
    override fun close() {
        this.checkConfigInfoExecutor.shutdown()
        this.longPollingExecutor.shutdown()
    }

    /**
     * 启动长轮询任务, 检查是否需要扩容LongPollingRunnable任务?
     * 默认情况下, 一个LongPollingRunnable需要去处理3000个CacheData的配置文件;
     * 如果超过了3000个CacheData, 那么就需要扩容LongPollingRunnable;
     */
    open fun checkConfigInfo() {
        val size = cacheMap.size

        // 计算需要的LongPollingTask的数量, 使用CacheData的size/perTaskConfigSize, 去进行向上取整得到
        val longPollingTaskCount = ceil(size.toDouble() / ParamUtils.getPerTaskConfigSize().toDouble()).roundToInt()

        // 如果需要的LongPollingTask的数量比当前的LongPollingTask的数量多, 那么说明需要去进行扩容
        if (longPollingTaskCount > currentLongingTaskCount) {

            // 扩容LongPollingTask到预期LongPollingTask的数量
            for (index in currentLongingTaskCount until longPollingTaskCount) {
                longPollingExecutor.execute(LongPollingRunnable(index))
            }

            // 修改当前的LongPollingTask的数量...
            currentLongingTaskCount = longPollingTaskCount
        }
    }

    /**
     * 检查本地的配置信息
     *
     * @param cacheData CacheData
     */
    private fun checkLocalConfig(cacheData: CacheData) {

    }

    /**
     * 检查ConfigServer当中的文件是否发生了变更?
     *
     * @param cacheDataList 要去进行检查的ConfigClient当中的CacheData列表
     * @return ConfigServer当中相比本地ConfigClient的配置文件, 发生变更的那些GroupKey信息
     */
    private fun checkUpdateDataIds(cacheDataList: List<CacheData>): List<String> {
        if (cacheDataList.isEmpty()) {
            return emptyList()
        }
        val builder = StringBuilder()

        // 把本地的这些CacheData的dataId、group、tenant、Md5传输给ConfigServer,
        // 让ConfigServer去检查已经发生变更的那些配置文件, 并给我们返回
        // 如果长度为3, 那么分别是dataId/group/md5;
        // 如果长度为4, 那么分别为dataId/group/md5/tenant
        cacheDataList.forEach {
            builder.append(it.group).append(Constants.WORD_SEPARATOR)
            builder.append(it.dataId).append(Constants.WORD_SEPARATOR)
            if (it.tenant.isNotBlank()) {
                builder.append(it.md5).append(Constants.WORD_SEPARATOR)
                builder.append(it.tenant)
            } else {
                builder.append(it.md5)
            }
            builder.append(Constants.LINE_SEPARATOR)
        }

        // ConfigServer当中的文件是否发生了变更?
        return checkUpdateConfigStr(builder.toString(), false)
    }

    /**
     * 检查ConfigServer当中配置文件，和本地的配置文件的MD5值去进行对比, 检查配置文件是否有发生变化?
     *
     * @param probeUpdateString 要去进行探查的配置文件的dataId&group&md5&tenant
     */
    open fun checkUpdateConfigStr(probeUpdateString: String, isInitializingCacheList: Boolean): List<String> {
        val headers = LinkedHashMap<String, String>()
        headers[ConfigConstants.LONG_POLLING_TIMEOUT_HEADER] = timeout.toString()
        val params = LinkedHashMap<String, String>()

        // 需要探测的可能更新的配置文件的groupKey列表
        params[Constants.LISTENING_CONFIGS] = probeUpdateString

        // readTimeout = 1.5*timeout, 为了避免Server处理任务的延时, 因此多加一段时间去等一会...
        val readTimout = timeout + timeout shr 1
        val result = agent.httpPost(
            Constants.CONFIG_CONTROLLER_PATH + "/listener",
            headers,
            params,
            Constants.ENCODE,
            readTimout
        )
        if (result.data == null) {
            return emptyList()
        }
        val updatedGroupKeys = ArrayList<String>()
        val data = URLDecoder.decode(result.data!!, Constants.ENCODE)
        data.split(Constants.LINE_SEPARATOR).forEach {
            val list = it.split(Constants.WORD_SEPARATOR)
            if (list.size == 2) {
                updatedGroupKeys += GroupKey.getKey(list[0], list[1])
            } else if (list.size == 3) {
                updatedGroupKeys += GroupKey.getKeyTenant(list[0], list[1], list[2])
            }
        }
        return updatedGroupKeys
    }

    /**
     * 从ConfigServer当中根据"dataId"&"group"&"tenant"去拉取对应的配置文件
     *
     * @param dataId dataId
     * @param group group
     * @param tenant tenant
     * @param timeout timeout
     * @return 拉取到的配置文件的内容
     */
    open fun getServerConfig(dataId: String, group: String, tenant: String, timeout: Long): ConfigResponse {
        val params = HashMap<String, String>()
        params[ConfigConstants.DATA_ID] = dataId
        params[ConfigConstants.GROUP] = group
        if (tenant.isNotBlank()) {
            params[ConfigConstants.TENANT] = tenant
        }
        val result = agent.httpGet(Constants.CONFIG_CONTROLLER_PATH, emptyMap(), params, Constants.ENCODE, timeout)
        val header = result.header

        val configType = header.getValue(Constants.CONFIG_TYPE) ?: ""
        val encryptedDataKey = header.getValue(Constants.ENCRYPTED_DATA_KEY) ?: ""
        val configResponse = ConfigResponse()
        configResponse.setContent(result.data)
        configResponse.setGroup(group)
        configResponse.setTenant(tenant)
        configResponse.setDataId(dataId)
        configResponse.setConfigType(configType)
        configResponse.setEncryptedDataKey(encryptedDataKey)
        return configResponse
    }

    /**
     * 长轮询任务的Runnable, 检查本地的这些配置文件，相比ConfigServer是否发生了变更?
     * 如果发生变更的话, 就需要去通知该CacheData去进行触发它的所有的Listener
     *
     * @param taskId 需要去进行处理的CacheData的taskId
     */
    inner class LongPollingRunnable(private val taskId: Int) : Runnable {
        override fun run() {
            val cacheDataList = ArrayList<CacheData>()

            // 统计出来所有的当前长轮询任务要去进行处理的CacheData
            cacheMap.values.forEach {
                if (taskId == it.taskId) {
                    // 统计出来当前长轮询需要去进行处理的CacheData
                    cacheDataList += it

                    // 检查本地的配置信息
                    checkLocalConfig(it)

                    // 检查Md5是否发生变更, 如果Md5发生变更了的话, 需要回调所有的Listener...
                    it.checkListenerMd5()
                }
            }

            // 检查ConfigServer当中的文件变更, 如果发生变更了的话, 那么需要返回发生变更的GroupKey(dataId&group&tenant)
            val changedGroupKeyList = checkUpdateDataIds(cacheDataList)


            // 统计出来所有的发生变更的Config配置文件的group&dataId&tenant
            // 重新从ConfigServer当中去加载配置文件...去刷新本地CacheData的内容和Md5
            changedGroupKeyList.forEach {
                val key = GroupKey.parseKey(it)
                val group = key[0]
                val dataId = key[1]
                val tenant = key[2]

                // 根据dataId&group&tenant从ConfigSever当中去拉取该配置文件的相关信息
                val serverConfig = getServerConfig(dataId, group, tenant, 3000L)

                // 根据dataId&group&tenant生成组合的GroupKey, 从cacheMap当中获取到对应的CacheData
                val groupKey = GroupKey.getKeyTenant(dataId, group, tenant)
                val cacheData = cacheMap[groupKey]!!

                // 将CacheData的文件内容去修改为新的ConfigServer当中的content, 同步修改MD5
                cacheData.content = serverConfig.getContent() ?: ""
                // 设置fileType
                cacheData.fileType = serverConfig.getConfigType() ?: "text"
            }

            // 在加载完成ConfigServer的配置文件之后, 继续检查一下Md5, 触发Listener
            // 因为在加载到ConfigServer的配置文件时, 会将CacheData当中的MD5去进行修改...
            cacheDataList.forEach {
                it.checkListenerMd5()
            }

            // re execute
            longPollingExecutor.execute(this)
        }
    }
}