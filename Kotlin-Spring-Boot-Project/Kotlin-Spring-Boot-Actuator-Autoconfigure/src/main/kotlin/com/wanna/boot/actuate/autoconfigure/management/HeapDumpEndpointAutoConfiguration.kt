package com.wanna.boot.actuate.autoconfigure.management

import com.wanna.boot.actuate.management.HeapDumpWebEndpoint
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 负责去对外暴露HeapDump的相关信息的自动配置类
 */
@Configuration(proxyBeanMethods = false)
open class HeapDumpEndpointAutoConfiguration {

    /**
     * 对外去暴露一个HeapDump的Endpoint
     */
    @Bean
    @ConditionalOnMissingBean
    open fun heapDumpWebEndpoint(): HeapDumpWebEndpoint = HeapDumpWebEndpoint()
}