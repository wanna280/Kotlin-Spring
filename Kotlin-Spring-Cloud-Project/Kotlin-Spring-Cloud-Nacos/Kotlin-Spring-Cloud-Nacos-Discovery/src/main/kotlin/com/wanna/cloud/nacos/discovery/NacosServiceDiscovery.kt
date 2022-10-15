package com.wanna.cloud.nacos.discovery

import com.alibaba.nacos.api.naming.NamingService
import com.alibaba.nacos.api.naming.pojo.Instance
import com.wanna.cloud.client.ServiceInstance
import com.wanna.cloud.nacos.NacosDiscoveryProperties
import com.wanna.cloud.nacos.NacosServiceInstance
import com.wanna.cloud.nacos.NacosServiceManager

/**
 * 整合NacosDiscoveryProperties和NacosServiceManager去实现Nacos的服务发现
 *
 * @see NacosServiceManager
 * @see NacosDiscoveryProperties
 * @see NacosDiscoveryClient
 */
open class NacosServiceDiscovery(
    private val properties: NacosDiscoveryProperties, private val manager: NacosServiceManager
) {
    /**
     * 获取所有已知服务的ServiceId列表
     *
     * @return serviceId列表
     */
    open fun getServices(): List<String> {
        return namingService().getServicesOfServer(1, Int.MAX_VALUE, properties.group).data
    }

    /**
     * 给定一个serviceId，去获取到该Service对应的所有的实例
     *
     * @param serviceId
     * @return 该serviceId对应的ServiceInstance列表
     */
    open fun getInstances(serviceId: String): List<ServiceInstance> {
        // 获取到所有的健康状态的ServiceInstance列表
        val instances = namingService().selectInstances(serviceId, properties.group, true)
        return hostToServiceInstanceList(instances, serviceId)
    }

    /**
     * 获取NamingService
     */
    private fun namingService(): NamingService {
        return manager.getNamingService(properties.getNacosProperties())
    }

    companion object {
        /**
         * 将Nacos的Instance列表，转换为适配SpringCloud的ServiceInstance列表
         *
         * @param instances Nacos的Instance列表
         * @param serviceId serviceId
         * @return 转换成为适配为SpringCloud的ServiceInstance列表
         */
        @JvmStatic
        fun hostToServiceInstanceList(instances: List<Instance>, serviceId: String): List<ServiceInstance> {
            return instances.map { hostToServiceInstance(it, serviceId) }.filterNotNull().toList()
        }

        /**
         * 将Nacos的Instance对象，转换为适配SpringCloud的ServiceInstance对象
         *
         * @param instance Nacos的Instance列表
         * @param serviceId serviceId
         * @return 适配为SpringCloud的ServiceInstance(有可能为null)
         */
        @JvmStatic
        fun hostToServiceInstance(instance: Instance, serviceId: String): ServiceInstance? {
            if (!instance.isEnabled && !instance.isHealthy) {
                return null
            }
            val serviceInstance = NacosServiceInstance()
            // 设置ServiceInstance相关的属性
            serviceInstance.setServiceId(serviceId)
            serviceInstance.setHost(instance.ip)
            serviceInstance.setPort(instance.port)
            serviceInstance.setInstanceId(instance.instanceId)

            // 构建相关的metadata元信息...
            val metadata = HashMap<String, String>()
            metadata["nacos.instanceId"] = instance.instanceId
            metadata["nacos.weight"] = instance.weight.toString()
            metadata["nacos.healthy"] = instance.isHealthy.toString()
            metadata["nacos.cluster"] = instance.clusterName
            metadata["nacos.ephemeral"] = instance.isEphemeral.toString()

            // merge Instance Metadata
            if (instance.metadata != null && instance.metadata.isNotEmpty()) {
                metadata += instance.metadata
            }
            serviceInstance.setMetadata(metadata)
            return serviceInstance
        }
    }

}