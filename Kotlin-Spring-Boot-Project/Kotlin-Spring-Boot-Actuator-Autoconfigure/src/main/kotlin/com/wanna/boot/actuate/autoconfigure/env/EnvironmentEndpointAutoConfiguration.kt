package com.wanna.boot.actuate.autoconfigure.env

import com.wanna.boot.actuate.env.EnvironmentEndpoint
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 暴露Spring的Environment(环境)信息的端点的自动配置类
 */
@Configuration(proxyBeanMethods = false)
open class EnvironmentEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun environmentEndpoint(environment: ConfigurableEnvironment): EnvironmentEndpoint =
        EnvironmentEndpoint(environment)
}