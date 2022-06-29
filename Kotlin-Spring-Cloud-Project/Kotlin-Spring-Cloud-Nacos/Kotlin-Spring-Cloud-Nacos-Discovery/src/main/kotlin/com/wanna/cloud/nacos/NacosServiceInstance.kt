package com.wanna.cloud.nacos

import com.wanna.cloud.client.ServiceInstance

/**
 * Nacos针对于SpringCloud的ServiceInstance的实现
 */
open class NacosServiceInstance : ServiceInstance {

    private var serviceId: String? = null

    private var instanceId: String? = null

    private var host: String? = null

    private var port: Int = -1

    private var metadata: Map<String, String>? = null

    open fun setServiceId(serviceId: String) {
        this.serviceId = serviceId
    }

    override fun getServiceId(): String {
        return this.serviceId!!
    }

    override fun getInstanceId(): String {
        return this.instanceId!!
    }

    open fun setInstanceId(instanceId: String) {
        this.instanceId = instanceId
    }

    override fun getHost(): String {
        return this.host!!
    }

    open fun setHost(host: String) {
        this.host = host
    }

    override fun getPort(): Int {
        return this.port
    }

    open fun setPort(port: Int) {
        this.port = port
    }

    override fun getUri(): String {
        return "http://$host:$port"
    }

    override fun getMetadata(): Map<String, String> {
        return this.metadata!!
    }

    open fun setMetadata(metadata: Map<String, String>) {
        this.metadata = metadata
    }
}