package com.wanna.cloud.autoconfigure

import com.wanna.cloud.context.properties.ConfigurationPropertiesBeans
import com.wanna.cloud.context.properties.ConfigurationPropertiesRebinder
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 支持对于标注有`@ConfigurationProperties`注解的Rebinder的自动配置类,
 * 负责监听环境已经改变的事件, 对`@ConfigurationProperties`的Bean去进行刷新
 *
 * @see ConfigurationPropertiesRebinder
 * @see ConfigurationPropertiesBeans
 */
@Configuration(proxyBeanMethods = false)
open class ConfigurationPropertiesRebinderAutoConfiguration {

    /**
     * 需要一个用来去维护SpringBeanFactory当中所有的`@ConfigurationProperties`的Bean的列表的组件
     *
     * @return ConfigurationPropertiesBeans
     */
    @Bean
    open fun configurationPropertiesBeans(): ConfigurationPropertiesBeans {
        return ConfigurationPropertiesBeans()
    }

    /**
     * 负责监听环境改变的事件, 去对标注有`@ConfigurationProperties`注解的Bean去进行rebind
     *
     * @return ConfigurationPropertiesRebinder
     */
    @Bean
    open fun configurationPropertiesRebinder(beans: ConfigurationPropertiesBeans): ConfigurationPropertiesRebinder {
        return ConfigurationPropertiesRebinder(beans)
    }
}