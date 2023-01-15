package com.wanna.cloud.nacos.discovery

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.cloud.nacos.NacosDiscoveryProperties
import com.wanna.cloud.nacos.NacosServiceManager
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * Nacos的服务注册发现的自动配置类, 提供Nacos的服务发现功能的自动配置
 */
@Configuration(proxyBeanMethods = false)
open class NacosDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun nacosDiscoveryProperties() : NacosDiscoveryProperties {
        return NacosDiscoveryProperties()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun nacosServiceManager() : NacosServiceManager {
        return NacosServiceManager()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun nacosServiceDiscovery(manager: NacosServiceManager,properties: NacosDiscoveryProperties) : NacosServiceDiscovery {
        return NacosServiceDiscovery(properties, manager)
    }
}