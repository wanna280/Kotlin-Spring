package com.wanna.cloud.nacos.registry

import com.alibaba.nacos.api.naming.NamingMaintainService
import com.alibaba.nacos.api.naming.NamingService
import com.alibaba.nacos.api.naming.pojo.Instance
import com.wanna.cloud.client.serviceregistry.Registration
import com.wanna.cloud.client.serviceregistry.ServiceRegistry
import com.wanna.cloud.nacos.NacosDiscoveryProperties
import com.wanna.cloud.nacos.NacosServiceManager
import com.wanna.common.logging.LoggerFactory

/**
 * 这是Nacos对于SpringCloud的注册中心的实现, 负责去完成Nacos服务的注册和管理
 *
 * @param manager Nacos服务的管理器
 * @param properties Nacos服务发现的配置信息
 */
open class NacosServiceRegistry(
    private val manager: NacosServiceManager, private val properties: NacosDiscoveryProperties
) : ServiceRegistry<Registration> {

    companion object {
        /**
         * Logger
         */
        private val logger = LoggerFactory.getLogger(NacosServiceManager::class.java)

        private const val STATUS_UP = "UP"
        private const val STATUS_DOWN = "DOWN"
    }

    /**
     * 注册一个实例到Nacos的注册中心([NamingService])
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

    /**
     * 当服务注册中心关闭时, 关闭Nacos的[NamingService]
     *
     * @see NamingService
     */
    override fun close() {
        try {
            manager.nacosNamingServiceShutdown()
        } catch (ex: Throwable) {
            logger.error("Nacos namingService shutDown failed", ex)
        }
    }

    /**
     * 修改给定的ServiceInstance实例的状态信息, 需要修改Nacos注册中心当中的实例状态
     *
     * @param registration 要去进行修改状态的ServiceInstance的Registration
     * @param status 要修改成为的状态信息
     */
    override fun setStatus(registration: Registration, status: String) {
        if (status != STATUS_UP && status != STATUS_DOWN) {
            logger.error("can't support status $status,please choose UP or DOWN")
            return
        }
        val serviceId = registration.getServiceId()
        val instance = buildNacosInstanceFromRegistration(registration)
        instance.isEnabled = status != STATUS_DOWN

        try {
            namingMaintainService().updateInstance(serviceId, instance)
        } catch (ex: Throwable) {
            logger.error("update nacos instance status fail", ex)
        }
    }

    /**
     * 获取给定的ServiceInstance实例的状态信息, 需要从Nacos注册中心当中去获取状态信息
     *
     * @param registration ServiceInstance的Registration
     * @return 该实例的状态信息
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> getStatus(registration: Registration): T {
        val serviceId = registration.getServiceId()

        // 从Nacos当中去获取该服务对应的所有的实例列表, 如果ip和端口号都一致, 那么我们就返回该实例的状态...
        val instances = namingService().getAllInstances(serviceId)
        for (instance in instances) {
            if (registration.getHost().equals(instance.ip, true)
                && registration.getPort() == instance.port
            ) {
                return (if (instance.isEnabled) STATUS_UP else STATUS_DOWN) as T
            }
        }
        return null as T
    }

    /**
     * 获取Nacos原生的NamingMaintainService, 用于提供对于实例状态信息的修改功能
     *
     * @return NamingMaintainService
     */
    private fun namingMaintainService(): NamingMaintainService {
        return this.manager.getNamingMaintainService(properties.getNacosProperties())
    }

    /**
     * 获取Nacos原生的NamingService
     *
     * @return Nacos NamingService
     */
    private fun namingService(): NamingService {
        return manager.getNamingService(properties.getNacosProperties())
    }

    /**
     * 根据SpringCloud的Registration以及NacosDiscoveryProperties, 去进行构建Nacos的Instance对象
     *
     * @param registration SpringCloud的Registration
     * @return 根据Registration去构建好的NacosInstance
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

        // service metadata
        instance.metadata = registration.getMetadata()
        return instance
    }
}