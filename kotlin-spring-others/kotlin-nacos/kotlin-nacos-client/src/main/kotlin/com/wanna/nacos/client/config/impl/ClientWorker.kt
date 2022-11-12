package com.wanna.nacos.client.config.impl

import com.wanna.nacos.api.config.listener.Listener
import java.io.Closeable

/**
 * NacosConfigClient的长轮询任务的Worker
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
open class ClientWorker : Closeable {

    open fun addTenantListeners(dataId: String, group: String, listeners: List<Listener>) {

    }

    override fun close() {

    }
}