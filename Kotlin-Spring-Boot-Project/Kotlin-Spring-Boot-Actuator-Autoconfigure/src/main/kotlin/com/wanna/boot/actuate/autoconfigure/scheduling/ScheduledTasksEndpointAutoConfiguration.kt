package com.wanna.boot.actuate.autoconfigure.scheduling

import com.wanna.boot.actuate.scheduling.ScheduledTasksEndpoint
import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.scheduling.config.ScheduledTaskHolder

@Configuration(proxyBeanMethods = false)
open class ScheduledTasksEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean([ScheduledTaskHolder::class])  // Condition On Enable Scheduling
    open fun scheduledTasksEndpoint(holder: ScheduledTaskHolder): ScheduledTasksEndpoint {
        return ScheduledTasksEndpoint(holder)
    }
}