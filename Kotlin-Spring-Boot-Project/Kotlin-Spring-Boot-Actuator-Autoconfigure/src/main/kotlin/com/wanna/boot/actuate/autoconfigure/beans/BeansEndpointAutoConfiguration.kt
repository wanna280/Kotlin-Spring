package com.wanna.boot.actuate.autoconfigure.beans

import com.wanna.boot.actuate.beans.BeansEndpoint
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * BeansEndpoint的自动配置类
 */
@Configuration(proxyBeanMethods = false)
open class BeansEndpointAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun beansEndpoint(applicationContext: ConfigurableApplicationContext): BeansEndpoint {
        return BeansEndpoint(applicationContext)
    }
}