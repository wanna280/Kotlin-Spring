package com.wanna.cloud.autoconfigure

import com.wanna.cloud.context.properties.ConfigurationPropertiesBeans
import com.wanna.cloud.context.properties.ConfigurationPropertiesRebinder
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 支持ConfigurationProperties的Rebinder的自动配置类, 负责监听环境已经改变的事件, 对@ConfigurationProperties的Bean去进行刷新
 */
@Configuration(proxyBeanMethods = false)
open class ConfigurationPropertiesRebinderAutoConfiguration {

    /**
     * 需要一个用来去维护@ConfigurationProperties的Bean的列表的组件
     */
    @Bean
    open fun configurationPropertiesBeans(): ConfigurationPropertiesBeans {
        return ConfigurationPropertiesBeans()
    }

    /**
     * 负责监听环境改变的事件, 去对@ConfigurationProperties1的Bean去进行rebind
     */
    @Bean
    open fun configurationPropertiesRebinder(beans: ConfigurationPropertiesBeans): ConfigurationPropertiesRebinder {
        return ConfigurationPropertiesRebinder(beans)
    }
}