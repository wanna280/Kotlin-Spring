package com.wanna.cloud.client.discovery

import com.wanna.cloud.client.ServiceInstance
import com.wanna.framework.core.Ordered

/**
 * 标识这是一个DiscoveryClient, 提供对于服务发现的服务列表的可读操作, 例如Nacos、Eureka
 */
interface DiscoveryClient : Ordered {
    companion object {
        const val DEFAULT_ORDER = 0
    }

    /**
     * 获取所有已知服务的ServiceId列表
     *
     * @return serviceId列表
     */
    fun getServices(): List<String>

    /**
     * 获取描述信息
     */
    fun getDescription(): String

    /**
     * 给定一个serviceId, 去获取到该Service对应的所有的实例
     *
     * @param serviceId
     * @return 该serviceId对应的ServiceInstance列表
     */
    fun getInstances(serviceId: String): List<ServiceInstance>

    override fun getOrder(): Int = DEFAULT_ORDER
}