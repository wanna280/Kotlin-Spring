package com.wanna.nacos.client.config

import com.wanna.nacos.api.config.ConfigService
import com.wanna.nacos.api.config.listener.Listener
import com.wanna.nacos.client.config.impl.ClientWorker

/**
 * Nacos的[ConfigService]的实现, 每个[NacosConfigService]负责去处理一个Namespace下的配置文件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
open class NacosConfigService : ConfigService {

    /**
     * 当前的NacosConfigService需要去进行处理的namespace
     */
    private var namespace = ""

    /**
     * Worker, LongPolling
     */
    private var worker: ClientWorker = ClientWorker()

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

    private fun publishConfigInner(
        namespace: String,
        dataId: String,
        group: String,
        content: String,
        fileType: String
    ) {

    }

    private fun getConfigInner(namespace: String, dataId: String, group: String, timeoutMs: Long): String {
        return ""
    }
}