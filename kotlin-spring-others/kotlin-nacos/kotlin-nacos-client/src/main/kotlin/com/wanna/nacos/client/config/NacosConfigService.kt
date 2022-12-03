package com.wanna.nacos.client.config

import com.wanna.nacos.api.PropertyKeyConst
import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.api.config.ConfigService
import com.wanna.nacos.api.config.listener.Listener
import com.wanna.nacos.api.exception.NacosException
import com.wanna.nacos.api.model.HttpRestResult
import com.wanna.nacos.client.config.common.ConfigConstants
import com.wanna.nacos.client.config.filter.impl.ConfigFilterChainManager
import com.wanna.nacos.client.config.filter.impl.ConfigRequest
import com.wanna.nacos.client.config.filter.impl.ConfigResponse
import com.wanna.nacos.client.config.http.HttpAgent
import com.wanna.nacos.client.config.http.ServerHttpAgent
import com.wanna.nacos.client.config.impl.ClientWorker
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * Nacos的[ConfigService]的实现, 每个[NacosConfigService]负责去处理一个Namespace下的配置文件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 *
 * @param properties 针对于ConfigService的配置信息
 * @see PropertyKeyConst
 */
open class NacosConfigService(private val properties: Properties) : ConfigService {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(NacosConfigService::class.java)

        /**
         * Post请求的超时时间
         */
        private const val POST_TIMEOUT = 3000L
    }

    /**
     * 根据Properties去初始化HttpAgent,
     * HttpAgent用于代理, 向ConfigServer当中去发送HTTP请求, 从而对ConfigServer当中的配置文件去进行增删改等操作
     */
    private val agent: HttpAgent = ServerHttpAgent(properties)

    /**
     * 当前的NacosConfigService需要去进行处理的namespace, 从Properties当中的"namespace"属性当中去进行获取
     */
    private var namespace = ""

    /**
     * 编码方式为UTF-8
     */
    private var encode = Constants.ENCODE

    /**
     * ConfigFilterChain的管理器
     */
    private val configFilterChainManager = ConfigFilterChainManager(properties)

    /**
     * ClientWorker, 提供LongPolling长轮询任务去拉取ConfigServer的配置文件
     */
    private var worker = ClientWorker(properties)

    init {
        // 根据Properties配置信息去初始化namespace
        initNamespace(properties)
    }

    /**
     * 根据Properties配置信息去初始化namespace
     *
     * @param properties Properties配置信息
     */
    private fun initNamespace(properties: Properties) {
        namespace = properties[PropertyKeyConst.NAMESPACE] as String? ?: ""
        properties[PropertyKeyConst.NAMESPACE] = namespace
    }

    override fun getConfig(dataId: String, group: String, timeoutMs: Long): String {
        return getConfigInner(namespace, dataId, group, timeoutMs)
    }

    override fun addListener(dataId: String, group: String, listener: Listener) {
        worker.addTenantListeners(dataId, group, listOf(listener))
    }

    override fun removeListener(dataId: String, group: String, listener: Listener) {
        worker.removeTenantListener(dataId, group, listener)
    }

    override fun publishConfig(dataId: String, group: String, content: String): Boolean {
        return publishConfig(dataId, group, content, Constants.DEFAULT_CONFIG_TYPE)
    }

    override fun publishConfig(dataId: String, group: String, content: String, fileType: String): Boolean {
        return publishConfigInner(namespace, dataId, group, content, fileType)
    }

    override fun removeConfig(dataId: String, group: String): Boolean {
        return removeConfigInner(namespace, dataId, group)
    }

    /**
     * 根据tenant/dataId/group去移除一个配置文件ConfigFile
     *
     * @param tenant namespace(tenant)
     * @param dataId dataId
     * @param group group
     * @return 移除配置文件是否成功? 移除成功return true; 移除失败return false
     */
    private fun removeConfigInner(tenant: String, dataId: String, group: String): Boolean {
        val result: HttpRestResult<String>
        val params = LinkedHashMap<String, String>()
        params[ConfigConstants.DATA_ID] = dataId
        params[ConfigConstants.GROUP] = dataId
        params[ConfigConstants.TENANT] = tenant
        try {
            result = agent.httpDelete(Constants.CONFIG_CONTROLLER_PATH, emptyMap(), params, encode, POST_TIMEOUT)
        } catch (ex: Exception) {
            logger.error("移除配置文件失败[dataId=$dataId, group=$group, tenant=$tenant]", ex)
            return false
        }
        if (result.ok()) {
            logger.info("移除配置文件成功[dataId=$dataId, group=$group, tenant=$tenant], msg=${result.message}")
            return true
        }
        return false
    }

    /**
     * 发布配置文件ConfigFile到ConfigServer
     *
     * @param namespace namespace
     * @param dataId dataId
     * @param group group
     * @param content content
     * @param fileType fileType
     * @return 如果发布配置文件成功, 那么return true; 否则return false
     */
    private fun publishConfigInner(
        namespace: String,
        dataId: String,
        group: String,
        content: String,
        fileType: String
    ): Boolean {
        val params = LinkedHashMap<String, String>()
        params[ConfigConstants.DATA_ID] = dataId
        params[ConfigConstants.GROUP] = group
        params[ConfigConstants.TENANT] = namespace
        params[ConfigConstants.FILE_TYPE] = fileType
        params[ConfigConstants.CONTENT] = content
        agent.httpPost(Constants.CONFIG_CONTROLLER_PATH, emptyMap(), params, encode, POST_TIMEOUT)
        return true
    }

    /**
     * 从ConfigServer当中根据dataId&group&tenant去拉取到配置文件
     *
     * @param namespace namespace
     * @param dataId dataId
     * @param group group
     * @param timeoutMs 超时时间(ms)
     * @return 从ConfigServer当中拉取到的配置文件的内容
     */
    private fun getConfigInner(namespace: String, dataId: String, group: String, timeoutMs: Long): String {
        val configResponse = ConfigResponse()
        configResponse.setDataId(dataId)
        configResponse.setTenant(namespace)
        configResponse.setGroup(group)
        try {
            val serverConfig = worker.getServerConfig(dataId, group, namespace, timeoutMs)
            configResponse.setContent(serverConfig.getContent())
            configResponse.setEncryptedDataKey(serverConfig.getEncryptedDataKey())

            configFilterChainManager.doFilter(ConfigRequest(), configResponse)

            return configResponse.getContent() ?: ""
        } catch (ex: NacosException) {
            logger.error("get ServerConfig Error", ex)
            throw ex
        }
    }

    /**
     * 关闭当前的ConfigService, 需要去关闭Worker的长轮询线程池以及HttpAgent
     */
    override fun shutdown() {
        this.worker.close()
        this.agent.close()
    }
}