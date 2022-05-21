package com.wanna.cloud.nacos.ribbon

import com.alibaba.nacos.api.naming.NamingService
import com.alibaba.nacos.api.naming.pojo.Instance
import com.netflix.client.config.IClientConfig
import com.netflix.loadbalancer.AbstractServerList
import com.wanna.cloud.nacos.NacosDiscoveryProperties
import com.wanna.cloud.nacos.NacosServiceManager

/**
 * 这是Nacos针对于Ribbon去进行实现的ServerList
 */
open class NacosServerList(
    private val discoveryProperties: NacosDiscoveryProperties,
    private val nacosServiceManager: NacosServiceManager
) :
    AbstractServerList<NacosServer>() {

    private var serviceId: String? = null

    override fun getInitialListOfServers(): MutableList<NacosServer> {
        return getServers()
    }

    override fun getUpdatedListOfServers(): MutableList<NacosServer> {
        return getServers()
    }

    private fun getServers(): MutableList<NacosServer> {
        val group = discoveryProperties.group
        val instances = namingService().selectInstances(serviceId, group, true)
        return toNacosServerList(instances)
    }

    /**
     * 将NacosInstance列表，转换为Ribbon的ServerList
     *
     * @param instances nacos实例列表
     */
    private fun toNacosServerList(instances: List<Instance>): MutableList<NacosServer> {
        return instances.map { NacosServer(it) }.toMutableList()
    }

    override fun initWithNiwsConfig(clientConfig: IClientConfig) {
        this.serviceId = clientConfig.clientName
    }

    private fun namingService(): NamingService {
        return nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties())
    }
}