package com.wanna.cloud.nacos.discovery

import com.wanna.cloud.client.ServiceInstance
import com.wanna.cloud.client.discovery.DiscoveryClient

/**
 * 这是一个Nacos的服务发现的Client, 实现SpringCloud当中对于DiscoveryClient的规范;
 * 它主要整合了NacosServiceDiscovery去进行委托实现SpringCloud的规范, 从而最终实现服务的发现;
 * 支持去获取所有的实例列表、根据serviceId去获取所有的ServiceInstance列表等功能
 *
 * @see NacosServiceDiscovery
 *
 * @param discovery Nacos的服务发现对象
 */
open class NacosDiscoveryClient(private val discovery: NacosServiceDiscovery) : DiscoveryClient {
    companion object {

        /**
         * Nacos DiscoveryClient的描述信息
         */
        private const val DESCRIPTION = "Spring Cloud Nacos Discovery Client"
    }

    /**
     * 获取所有的服务的serviceId列表
     */
    override fun getServices(): List<String> {
        return discovery.getServices()
    }

    /**
     * Nacos DiscoveryClient的描述信息
     *
     * @return description
     */
    override fun getDescription() = DESCRIPTION

    /**
     * 根据serviceId去获取到所有的ServiceInstance
     *
     * @param serviceId serviceId
     * @return ServiceInstance列表
     */
    override fun getInstances(serviceId: String): List<ServiceInstance> {
        return discovery.getInstances(serviceId)
    }
}