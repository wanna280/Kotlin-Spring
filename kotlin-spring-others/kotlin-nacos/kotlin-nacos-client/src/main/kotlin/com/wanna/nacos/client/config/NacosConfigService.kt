package com.wanna.nacos.client.config

import com.wanna.framework.web.client.RestTemplate
import com.wanna.nacos.api.PropertyKeyConst
import com.wanna.nacos.api.config.ConfigService
import com.wanna.nacos.api.config.listener.Listener
import com.wanna.nacos.api.exception.NacosException
import com.wanna.nacos.client.config.filter.impl.ConfigResponse
import com.wanna.nacos.client.config.filter.impl.ConfigFilterChainManager
import com.wanna.nacos.client.config.filter.impl.ConfigRequest
import com.wanna.nacos.client.config.impl.ClientWorker
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Nacos的[ConfigService]的实现, 每个[NacosConfigService]负责去处理一个Namespace下的配置文件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 *
 * @param properties ConfigService的配置信息
 */
open class NacosConfigService(private val properties: Properties) : ConfigService {

    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(NacosConfigService::class.java)

    /**
     * RestTemplate
     */
    private val restTemplate = RestTemplate()


    /**
     * 当前的NacosConfigService需要去进行处理的namespace
     */
    private var namespace = ""

    /**
     * ConfigFilterChain的管理器
     */
    private val configFilterChainManager = ConfigFilterChainManager(properties)

    /**
     * Worker, 提供LongPolling去拉取ConfigServer的配置文件
     */
    private var worker: ClientWorker = ClientWorker(properties)

    init {
        // 初始化namespace
        initNamespace(properties)
    }

    /**
     * 初始化namespace
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

    override fun publishConfig(dataId: String, group: String, content: String) {
        publishConfig(dataId, group, content, "txt")
    }

    override fun publishConfig(dataId: String, group: String, content: String, fileType: String) {
        publishConfigInner(namespace, dataId, group, content, fileType)
    }

    /**
     * 发布配置文件ConfigFile
     *
     * @param namespace namespace
     * @param dataId dataId
     * @param group group
     * @param content content
     * @param fileType fileType
     */
    private fun publishConfigInner(
        namespace: String,
        dataId: String,
        group: String,
        content: String,
        fileType: String
    ) {
        restTemplate.postForEntity("/v1/cs/configs", String::class.java, emptyMap())
    }

    /**
     * 从ConfigServer当中拉取配置文件
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
}