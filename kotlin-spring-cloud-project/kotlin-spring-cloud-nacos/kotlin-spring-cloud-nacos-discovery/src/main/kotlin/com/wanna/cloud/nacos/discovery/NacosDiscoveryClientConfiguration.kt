package com.wanna.cloud.nacos.discovery

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 提供Nacos的DiscoveryClient, 支持利用DiscoveryClient去完成服务的发现
 */
@Configuration(proxyBeanMethods = false)
open class NacosDiscoveryClientConfiguration {

    /**
     * 为容器当中导入一个NacosDiscoveryClient, 去支持服务的发现
     *
     * @param nacosServiceDiscovery NacosServiceDiscovery, 提供Nacos的服务发现
     */
    @Bean
    @ConditionalOnMissingBean
    open fun nacosDiscoveryClient(nacosServiceDiscovery: NacosServiceDiscovery): NacosDiscoveryClient {
        return NacosDiscoveryClient(nacosServiceDiscovery)
    }
}