package com.wanna.cloud.nacos.registry

import com.wanna.cloud.client.ServiceInstance
import com.wanna.cloud.client.serviceregistry.Registration
import com.wanna.cloud.nacos.NacosDiscoveryProperties
import javax.annotation.PostConstruct

/**
 * 这是一个Nacos的Registration
 */
open class NacosRegistration(
    private val customizers: List<NacosRegistrationCustomizer>,
    val properties: NacosDiscoveryProperties
) : Registration, ServiceInstance {

    @PostConstruct
    open fun init() {
        val metadata = getMetadata()

        if (properties.heartBeatInterval != -1) {
            metadata["preserved.heart.beat.interval"] = properties.heartBeatInterval.toString()
        }
        if (properties.heartBeatTimeout != -1) {
            metadata["preserved.heart.beat.timeout"] = properties.heartBeatTimeout.toString()
        }
        if (properties.ipDeleteTimeout != -1) {
            metadata["preserved.ip.delete.timeout"] = properties.ipDeleteTimeout.toString()
        }

        // 遍历所有的初始化器, 去对NacosRegistration去进行自定义...
        if (this.customizers.isNotEmpty()) {
            this.customizers.forEach { it.customNacosRegistration(this) }
        }
    }

    override fun getServiceId(): String {
        return properties.service
    }

    override fun getHost(): String {
        return properties.ip
    }

    override fun getPort(): Int {
        return properties.port
    }

    override fun isSecure(): Boolean {
        return properties.secure;
    }

    override fun getUri(): String {
        val schema = if (isSecure()) "https" else "http"
        return "$schema://${properties.ip}:${properties.port}"
    }

    override fun getMetadata(): MutableMap<String, String> {
        return properties.metadata
    }
}