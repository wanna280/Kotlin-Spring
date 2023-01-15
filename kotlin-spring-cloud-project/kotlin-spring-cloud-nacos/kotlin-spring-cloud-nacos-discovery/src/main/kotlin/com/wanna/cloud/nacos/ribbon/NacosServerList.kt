package com.wanna.cloud.nacos.ribbon

import com.alibaba.nacos.api.naming.NamingService
import com.alibaba.nacos.api.naming.pojo.Instance
import com.netflix.client.config.IClientConfig
import com.netflix.loadbalancer.AbstractServerList
import com.wanna.cloud.nacos.NacosDiscoveryProperties
import com.wanna.cloud.nacos.NacosServiceManager

/**
 * 这是Nacos针对于Ribbon去进行实现的ServerList, 主要是为Ribbon的负载均衡去提供Server的选择来源; 
 *
 * @see AbstractServerList
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

    /**
     * 获取Nacos注册中心当中的所有NacosServer实例, 需要使用group+serviceId, 联合去进行查询
     *
     * @return 从NacosServer当中获取到的所有的NacosServer(Nacos Instance)列表
     */
    private fun getServers(): MutableList<NacosServer> {
        val group = discoveryProperties.group
        val instances = namingService().selectInstances(serviceId, group, true)
        return toNacosServerList(instances)
    }

    /**
     * 将NacosInstance列表, 转换为Ribbon的ServerList
     *
     * @param instances nacos实例列表
     */
    private fun toNacosServerList(instances: List<Instance>): MutableList<NacosServer> {
        return instances.map { NacosServer(it) }.toMutableList()
    }

    /**
     * 初始化相关的配置, 这里主要是初始化serviceId(clientName), Ribbon会自动自动把相关的信息保存到IClientConfig当中
     *
     * @param clientConfig ClientConfig相关信息
     */
    override fun initWithNiwsConfig(clientConfig: IClientConfig) {
        this.serviceId = clientConfig.clientName
    }

    private fun namingService(): NamingService {
        return nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties())
    }
}