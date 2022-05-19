package com.wanna.cloud.nacos

import com.wanna.boot.autoconfigure.condition.ConditionOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
open class NacosServiceAutoConfiguration {

    @Bean
    @ConditionOnMissingBean
    open fun nacosServiceManager() : NacosServiceManager {
        return NacosServiceManager()
    }
}