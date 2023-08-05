package com.wanna.boot.actuate.autoconfigure.management

import com.wanna.boot.actuate.management.ThreadDumpEndpoint
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 线程的Dump信息的自动配置类, 它负责去对外提供线程的Dump信息
 *
 * @see ThreadDumpEndpoint
 */
@Configuration(proxyBeanMethods = false)
open class ThreadDumpEndpointAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun threadDumpEndpoint(): ThreadDumpEndpoint = ThreadDumpEndpoint()
}