package com.wanna.boot.actuate.autoconfigure.scheduling

import com.wanna.boot.actuate.scheduling.ScheduledTasksEndpoint
import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.scheduling.config.ScheduledTaskHolder

/**
 * 对于定时任务的Endpoint的监控信息的暴露的自动配置类, 可以对外查看Spring当中的定时任务相关的信息
 *
 * @see ScheduledTasksEndpoint
 */
@Configuration(proxyBeanMethods = false)
open class ScheduledTasksEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean([ScheduledTaskHolder::class])  // Condition On Enable Scheduling
    open fun scheduledTasksEndpoint(holder: ScheduledTaskHolder): ScheduledTasksEndpoint =
        ScheduledTasksEndpoint(holder)
}