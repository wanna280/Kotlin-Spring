package com.wanna.boot.actuate.autoconfigure.logging

import com.wanna.boot.actuate.logging.LoggersEndpoint
import com.wanna.boot.autoconfigure.condition.ConditionOutcome
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.condition.SpringBootCondition
import com.wanna.boot.logging.LoggerGroups
import com.wanna.boot.logging.LoggingSystem
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.context.annotation.Conditional
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.type.AnnotatedTypeMetadata
import java.util.*

/**
 * [LoggersEndpoint]的自动配置类, 暴露关于[LoggingSystem]相关信息的Endpoint
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 *
 * @see LoggersEndpoint
 */
@Configuration(proxyBeanMethods = false)
open class LoggersEndpointAutoConfiguration {

    /**
     * 给Spring BeanFactory当中去装配一个[LoggersEndpoint]
     *
     * @param loggerGroups LoggerGroups
     * @param loggingSystem LoggingSystem
     * @return LoggersEndpoint
     */
    @Bean
    @Conditional(OnEnabledLoggingSystemCondition::class)
    @ConditionalOnMissingBean
    open fun loggersEndpoint(loggingSystem: LoggingSystem, loggerGroups: Optional<LoggerGroups>): LoggersEndpoint {
        return LoggersEndpoint(loggingSystem, loggerGroups.orElse(LoggerGroups()))
    }

    /**
     * 检查[LoggingSystem]是否启用的[SpringBootCondition]
     */
    private class OnEnabledLoggingSystemCondition : SpringBootCondition() {
        override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
            val environment = context.getEnvironment()
            val loggingSystem = environment.getProperty(LoggingSystem.SYSTEM_PROPERTY)
            if (loggingSystem == LoggingSystem.NONE) {
                ConditionOutcome.noMatch("Logging System no match because system property ${LoggingSystem.SYSTEM_PROPERTY} is set to none")
            }
            return ConditionOutcome.match("Logging System enabled")
        }
    }
}