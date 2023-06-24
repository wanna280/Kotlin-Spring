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

    /**
     * Nacos的服务发现的相关的配置信息的SpringBean
     *
     * @return Nacos的服务发现的配置信息
     */
    @Bean
    @ConditionalOnMissingBean
    open fun nacosDiscoveryProperties(): NacosDiscoveryProperties {
        return NacosDiscoveryProperties()
    }

    /**
     * Nacos的原生API的NamingService的管理器
     *
     * @return NacosServiceManager
     */
    @Bean
    @ConditionalOnMissingBean
    open fun nacosServiceManager(): NacosServiceManager {
        return NacosServiceManager()
    }

    /**
     * Nacos的服务发现的SpringBean
     *
     * @param manager Nacos的NamingService的Manager
     * @param properties Nacos的服务发现的配置信息
     */
    @Bean
    @ConditionalOnMissingBean
    open fun nacosServiceDiscovery(
        manager: NacosServiceManager,
        properties: NacosDiscoveryProperties
    ): NacosServiceDiscovery {
        return NacosServiceDiscovery(properties, manager)
    }
}