package com.wanna.cloud.nacos.discovery

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 提供Nacos的DiscoveryClient, 支持利用DiscoveryClient去完成服务的发现
 *
 * @see NacosDiscoveryClient
 */
@Configuration(proxyBeanMethods = false)
open class NacosDiscoveryClientConfiguration {

    /**
     * 为容器当中导入一个[NacosDiscoveryClient], 去支持服务的发现,
     *
     * * 1.用于获取注册中心当中的全部的已经注册的服务列表的serviceId
     * * 2.用于根据serviceId, 去获取到该servieId对象的全部的实例列表
     *
     * @param nacosServiceDiscovery NacosServiceDiscovery, 提供Nacos的服务发现
     */
    @Bean
    @ConditionalOnMissingBean
    open fun nacosDiscoveryClient(nacosServiceDiscovery: NacosServiceDiscovery): NacosDiscoveryClient {
        return NacosDiscoveryClient(nacosServiceDiscovery)
    }
}