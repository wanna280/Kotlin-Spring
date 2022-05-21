package com.wanna.boot.autoconfigure.web.mvc

import com.wanna.boot.autoconfigure.condition.ConditionOnMissingBean
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.boot.web.reactive.server.ReactiveWebServerFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Primary
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.DispatcherHandlerImpl
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.config.annotation.DelegatingWebMvcConfiguration
import com.wanna.framework.web.method.annotation.RequestMappingHandlerMapping

@EnableConfigurationProperties([NettyWebServerProperties::class])
@Configuration(proxyBeanMethods = false)
open class NettyMvcAutoConfiguration : ApplicationContextAware {

    private var applicationContext: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    @Bean
    @ConditionOnMissingBean
    open fun dispatcherHandler(): DispatcherHandler {
        val dispatcherHandler = DispatcherHandlerImpl()
        dispatcherHandler.setApplicationContext(this.applicationContext!!)
        return dispatcherHandler
    }

    /**
     * 给容器中导入一个NettyWebServerFactory
     */
    @Bean
    open fun nettyWebServerFactory(properties: NettyWebServerProperties): ReactiveWebServerFactory {
        val nettyWebServerFactory = NettyWebServerFactory()
        nettyWebServerFactory.setHandler(NettyServerHandler(this.applicationContext!!))
        nettyWebServerFactory.setPort(properties.port)
        return nettyWebServerFactory
    }

    @Component
    open class EnableWebMvcConfiguration : DelegatingWebMvcConfiguration() {

        @Bean  // set to Primary
        @Primary
        override fun requestMappingHandlerMapping(contentNegotiationManager: ContentNegotiationManager): RequestMappingHandlerMapping {
            return super.requestMappingHandlerMapping(contentNegotiationManager)
        }
    }
}