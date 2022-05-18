package com.wanna.cloud.nacos.config

import com.wanna.cloud.bootstrap.config.PropertySourceLocator
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
open class NacosBootstrapConfiguration {

    @Bean
    fun nacosPropertySourceLocator(nacosConfigManager: NacosConfigManager): PropertySourceLocator {
        return NacosPropertySourceLocator(nacosConfigManager)
    }

    @Bean
    fun nacosConfigManager(nacosConfigProperties: NacosConfigProperties): NacosConfigManager {
        return NacosConfigManager(nacosConfigProperties)
    }

    @Bean
    fun nacosConfigProperties(): NacosConfigProperties {
        return NacosConfigProperties()
    }
}