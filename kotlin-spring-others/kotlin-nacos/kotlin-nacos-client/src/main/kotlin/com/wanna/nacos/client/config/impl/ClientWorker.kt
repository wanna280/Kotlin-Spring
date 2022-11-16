package com.wanna.nacos.client.config.impl

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.client.RequestCallback
import com.wanna.framework.web.client.ResponseExtractor
import com.wanna.framework.web.client.RestTemplate
import com.wanna.framework.web.http.ResponseEntity
import com.wanna.framework.web.http.client.ClientHttpRequest
import com.wanna.framework.web.http.client.ClientHttpResponse
import com.wanna.nacos.api.PropertyKeyConst
import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.api.config.listener.Listener
import com.wanna.nacos.client.config.filter.impl.ConfigResponse
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * NacosConfigClient的长轮询任务的Worker
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
     * RestTemplate
     */
    private val restTemplate = RestTemplate()

    /**
     * ConfigClient的客户端长轮询的timeout
     */
    private var timeout: Long = 0L

    /**
     * 维护所有的缓存数据
     */
    private var cacheMap = ConcurrentHashMap<String, CacheData>()

    /**
     * LongPolling ScheduledExecutorService
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
     * 检查配置信息的定时任务线程池, 每隔10ms去检查一下配置是否有发生变更?
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

        // 在初始化时, 就添加一个10s的定时任务去执行checkConfigInfo
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
     * 添加Listener
     *
     * @param dataId dataId
     * @param group group
     * @param listeners 需要添加的监听器列表
     */
    open fun addTenantListeners(dataId: String, group: String, listeners: List<Listener>) {
        val cacheData = addCacheDataIfAbsent(dataId, group, "")
        listeners.forEach(cacheData::addListener)
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
     * 启动长轮询任务, 检查ConfigInfo是否发生了变更?
     */
    open fun checkConfigInfo() {
        longPollingExecutor.execute(LongPollingRunnable(0))
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
        val builder = StringBuilder()

        // 把本地的这些CacheData的dataId、group、tenant、Md5传输给ConfigServer,
        // 让ConfigServer去检查已经发生变更的那些配置文件, 并给我们返回
        cacheDataList.forEach {
            builder.append(it.group).append(Char(2))
            builder.append(it.dataId).append(Char(2))
            if (it.tenant.isNotBlank()) {
                builder.append(it.md5).append(Char(2))
                builder.append(it.tenant).append("\n")
            } else {
                builder.append(it.md5).append("\n")
            }
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
        val entity = restTemplate.execute(URI("/v1/cs/configs/listener"), RequestMethod.GET, object : RequestCallback {
            override fun doWithRequest(request: ClientHttpRequest) {
                request.getHeaders().add("Long-Polling-Timeout", timeout.toString())
            }
        }, object : ResponseExtractor<ResponseEntity<String>> {
            override fun extractData(response: ClientHttpResponse): ResponseEntity<String>? {
                return ResponseEntity(
                    response.getStatusCode(),
                    response.getHeaders(),
                    String(response.getBody().readAllBytes())
                )
            }
        })!!


        // readTimeout = 1.5*timeout, 为了避免Server处理任务的延时, 因此多加一段时间去等一会...
        val readTimout = timeout + timeout shr 1

        val body = entity.body

        return emptyList()
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
        params["dataId"] = dataId
        params["group"] = group
        if (tenant.isNotBlank()) {
            params["tenant"] = tenant
        }

        val url = StringBuilder("http://localhost:9966/v1/cs/configs")
        if (params.isNotEmpty()) {
            url.append("?")
            params.forEach { url.append(it.key).append("=").append(it.value).append("&") }
            url.setLength(url.length - 1)  // remote last &
        }
        val entity = restTemplate.execute(
            URI(url.toString()),
            RequestMethod.GET,
            object : RequestCallback {
                override fun doWithRequest(request: ClientHttpRequest) {

                }
            },
            object : ResponseExtractor<ResponseEntity<String>> {
                override fun extractData(response: ClientHttpResponse): ResponseEntity<String> {
                    return ResponseEntity(
                        response.getStatusCode(),
                        response.getHeaders(),
                        String(response.getBody().readAllBytes())
                    )
                }
            })!!
        val headers = entity.headers
        val content = entity.body ?: ""

        val configType = headers.getFirst(Constants.CONFIG_TYPE) ?: ""
        val encryptedDataKey = headers.getFirst(Constants.ENCRYPTED_DATA_KEY) ?: ""
        val configResponse = ConfigResponse()
        configResponse.setContent(content)
        configResponse.setGroup(group)
        configResponse.setTenant(tenant)
        configResponse.setDataId(dataId)
        configResponse.setConfigType(configType)
        configResponse.setEncryptedDataKey(encryptedDataKey)
        return configResponse
    }

    /**
     * 长轮询任务的Runnable, 检查本地的这些配置文件，相比ConfigServer是否发生了变更?
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