package com.wanna.cloud.nacos

import com.wanna.cloud.client.ServiceInstance

/**
 * Nacos针对于SpringCloud的ServiceInstance的实现
 */
open class NacosServiceInstance : ServiceInstance {

    /**
     * 微服务的服务名称
     */
    private var serviceId: String? = null

    /**
     * 实例ID
     */
    private var instanceId: String? = null

    /**
     * host
     */
    private var host: String? = null

    /**
     * host
     */
    private var port: Int = -1

    /**
     * 是否应该使用https去进行注册
     */
    private var secure: Boolean = false;

    /**
     * 服务的Metadata元信息
     */
    private var metadata: Map<String, String>? = null

    open fun setServiceId(serviceId: String) {
        this.serviceId = serviceId
    }

    override fun getServiceId(): String {
        return this.serviceId!!
    }

    override fun getInstanceId(): String? {
        return this.instanceId
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

    override fun isSecure(): Boolean = secure

    open fun setSecure(secure: Boolean) {
        this.secure = secure
    }


    override fun getUri(): String {
        val schema = if (isSecure()) "https" else "http"
        return "$schema://$host:$port"
    }

    override fun getMetadata(): Map<String, String> {
        return this.metadata ?: emptyMap()
    }

    open fun setMetadata(metadata: Map<String, String>) {
        this.metadata = metadata
    }
}