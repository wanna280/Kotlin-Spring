package com.wanna.cloud.autoconfigure

import com.wanna.cloud.context.properties.ConfigurationPropertiesBeans
import com.wanna.cloud.context.properties.ConfigurationPropertiesRebinder
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 支持ConfigurationProperties的Rebinder的自动配置类
 */
@Configuration(proxyBeanMethods = false)
open class ConfigurationPropertiesRebinderAutoConfiguration {

    @Bean
    open fun configurationPropertiesBeans(): ConfigurationPropertiesBeans {
        return ConfigurationPropertiesBeans()
    }

    @Bean
    open fun configurationPropertiesRebinder(beans: ConfigurationPropertiesBeans): ConfigurationPropertiesRebinder {
        return ConfigurationPropertiesRebinder(beans)
    }
}