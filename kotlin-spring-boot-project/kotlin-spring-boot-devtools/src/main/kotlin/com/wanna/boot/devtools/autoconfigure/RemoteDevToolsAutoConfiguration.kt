package com.wanna.boot.devtools.autoconfigure

import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.boot.devtools.remote.server.RemoteServerHandlerMapping
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

@EnableConfigurationProperties([DevToolsProperties::class])
@Configuration(proxyBeanMethods = false)
open class RemoteDevToolsAutoConfiguration {

    @Bean
    open fun remoteServerHandlerMapping(): RemoteServerHandlerMapping {
        return RemoteServerHandlerMapping()
    }
}