package com.wanna.cloud.nacos.registry

import com.alibaba.nacos.api.naming.NamingService
import com.alibaba.nacos.api.naming.pojo.Instance
import com.wanna.cloud.client.serviceregistry.Registration
import com.wanna.cloud.client.serviceregistry.ServiceRegistry
import com.wanna.cloud.nacos.NacosDiscoveryProperties
import com.wanna.cloud.nacos.NacosServiceManager

/**
 * 这是Nacos对于SpringCloud的注册中心的实现，负责去完成Nacos服务的注册
 */
open class NacosServiceRegistry(
    private val manager: NacosServiceManager, private val properties: NacosDiscoveryProperties
) : ServiceRegistry<Registration> {

    companion object {
        private const val STATUS_UP = "UP"
        private const val STATUS_DOWN = "DOWN"
    }

    /**
     * 注册一个实例到Nacos的注册中心(NamingService)
     *
     * @param registration SpringCloud的Registration
     */
    override fun register(registration: Registration) {
        val serviceId = registration.getServiceId()
        val group = properties.group
        val instance = buildNacosInstanceFromRegistration(registration)

        // 注册一个NacosInstance到NamingService当中
        namingService().registerInstance(serviceId, group, instance)
    }

    /**
     * 从Nacos的注册中心(NamingService)取消注册一个实例
     *
     * @param registration SpringCloud的Registration
     */
    override fun deregister(registration: Registration) {
        namingService().deregisterInstance(
            registration.getServiceId(), properties.group, registration.getHost(), registration.getPort()
        )
    }

    override fun close() {

    }

    override fun setStatus(registration: Registration, status: String) {

    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getStatus(registration: Registration): T {
        return STATUS_UP as T
    }

    /**
     * 获取NamingService
     */
    private fun namingService(): NamingService {
        return manager.getNamingService(properties.getNacosProperties())
    }

    /**
     * 根据SpringCloud的Registration以及NacosDiscoveryProperties，去进行构建Nacos的Instance
     *
     * @param registration SpringCloud的Registration
     * @return 构建好的NacosInstance
     */
    private fun buildNacosInstanceFromRegistration(registration: Registration): Instance {
        val instance = Instance()
        instance.ip = registration.getHost()
        instance.port = registration.getPort()
        instance.metadata = registration.getMetadata()
        instance.isEphemeral = properties.ephemeral
        instance.weight = properties.weight
        instance.isEnabled = properties.instanceEnabled
        instance.clusterName = properties.clusterName
        return instance
    }
}