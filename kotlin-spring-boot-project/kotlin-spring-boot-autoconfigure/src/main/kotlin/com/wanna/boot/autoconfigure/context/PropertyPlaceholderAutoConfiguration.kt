package com.wanna.boot.autoconfigure.context

import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.support.PropertySourcesPlaceholderConfigurer

/**
 * 自动处理占位符的自动配置类
 */
@Configuration(proxyBeanMethods = false)
open class PropertyPlaceholderAutoConfiguration {

    @Bean
    open fun propertySourcesPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer {
        return PropertySourcesPlaceholderConfigurer()
    }
}