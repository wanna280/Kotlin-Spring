package com.wanna.cloud.nacos.ribbon

import com.wanna.cloud.netflix.ribbon.SpringClientFactory
import com.netflix.client.config.IClientConfig
import com.wanna.boot.autoconfigure.condition.ConditionOnMissingBean
import com.wanna.cloud.nacos.NacosDiscoveryProperties
import com.wanna.cloud.nacos.NacosServiceManager
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.cloud.context.named.NamedContextFactory

/**
 * Nacos的RibbonClient配置类，自定义定制ServerList，去进行自定义的负载均衡的Server实例的获取规则，这个类并不是直接交给Spring容器去进行管理；
 * 而是交给具体的RibbonClient去作为配置类去进行注册，保存到SpringClientFactory当中的childContext当中，通过serviceName作为key，可以去找到该childContext；
 *
 * @see SpringClientFactory
 * @see NamedContextFactory
 */
@Configuration(proxyBeanMethods = false)
open class NacosRibbonClientConfiguration {

    @Bean
    @ConditionOnMissingBean
    open fun nacosDiscoveryProperties() : NacosDiscoveryProperties {
        return NacosDiscoveryProperties()
    }

    @Bean
    @ConditionOnMissingBean
    open fun nacosServiceManager() : NacosServiceManager {
        return NacosServiceManager()
    }

    @Bean
    @ConditionOnMissingBean
    open fun nacosServerList(
        discoveryProperties: NacosDiscoveryProperties,
        manager: NacosServiceManager,
        config: IClientConfig
    ): NacosServerList {
        val nacosServerList = NacosServerList(discoveryProperties, manager)
        nacosServerList.initWithNiwsConfig(config)
        return nacosServerList
    }
}