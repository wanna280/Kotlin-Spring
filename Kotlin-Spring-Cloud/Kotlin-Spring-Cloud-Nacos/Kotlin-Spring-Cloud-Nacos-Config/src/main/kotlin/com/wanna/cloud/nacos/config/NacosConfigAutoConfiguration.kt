package com.wanna.cloud.nacos.config

import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class NacosConfigAutoConfiguration {

    @Bean
    fun nacosConfigProperties(): NacosConfigProperties {
        return NacosConfigProperties()
    }

    @Bean
    fun nacosConfigManager(nacosConfigProperties: NacosConfigProperties): NacosConfigManager {
        return NacosConfigManager(nacosConfigProperties)
    }

    @Bean
    fun nacosContextRefresher(nacosConfigManager: NacosConfigManager): NacosContextRefresher {
        return NacosContextRefresher(nacosConfigManager)
    }
}