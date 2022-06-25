package com.wanna.cloud.nacos

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
open class NacosServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun nacosServiceManager() : NacosServiceManager {
        return NacosServiceManager()
    }
}