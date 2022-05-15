package com.wanna.boot.autoconfigure.web.reactive

import com.wanna.boot.web.reactive.server.ReactiveWebServerFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Primary
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.DispatcherHandlerImpl
import com.wanna.framework.web.HandlerInterceptor
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.config.annotation.DelegatingWebMvcConfiguration
import com.wanna.framework.web.config.annotation.InterceptorRegistry
import com.wanna.framework.web.config.annotation.WebMvcConfigurationSupport
import com.wanna.framework.web.config.annotation.WebMvcConfigurer
import com.wanna.framework.web.method.annotation.RequestMappingHandlerMapping
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

@Configuration(proxyBeanMethods = false)
open class NettyReactiveAutoConfiguration : ApplicationContextAware {

    private var applicationContext: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    @Bean
    open fun dispatcherHandler(): DispatcherHandler {
        val dispatcherHandler = DispatcherHandlerImpl()
        dispatcherHandler.setApplicationContext(this.applicationContext!!)
        return dispatcherHandler
    }

    /**
     * 给容器中导入一个NettyWebServerFactory
     */
    @Bean
    open fun nettyWebServerFactory(): ReactiveWebServerFactory {
        val nettyWebServerFactory = NettyWebServerFactory()
        nettyWebServerFactory.setHandler(NettyServerHandler(this.applicationContext!!))
        return nettyWebServerFactory
    }

    @Bean
    fun configurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addInterceptors(registry: InterceptorRegistry) {
                registry.addInterceptor(object : HandlerInterceptor {
                    override fun preHandle(
                        request: HttpServerRequest,
                        response: HttpServerResponse,
                        handler: Any
                    ): Boolean {
                        println("pre")
                        return true
                    }

                    override fun postHandle(request: HttpServerRequest, response: HttpServerResponse, handler: Any) {
                        println("post")
                    }

                    override fun afterCompletion(
                        request: HttpServerRequest,
                        response: HttpServerResponse,
                        handler: Any,
                        ex: Throwable?
                    ) {
                        println("afterCompletion")
                    }
                })
            }
        }
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