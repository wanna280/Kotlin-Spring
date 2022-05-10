package com.wanna.boot.autoconfigure.web.reactive

import com.wanna.boot.web.reactive.server.ReactiveWebServerFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.DispatcherHandlerImpl
import com.wanna.framework.web.http.converter.json.MappingJackson2HttpMessageConverter
import com.wanna.framework.web.method.annotation.*
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolverComposite
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandlerComposite
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

@Configuration(proxyBeanMethods = false)
class NettyReactiveAutoConfiguration : ApplicationContextAware {

    private var applicationContext: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 给容器中导入一个NettyWebServerFactory
     */
    @Bean
    fun nettyWebServerFactory(): ReactiveWebServerFactory {
        val nettyWebServerFactory = NettyWebServerFactory()
        nettyWebServerFactory.setHandler(NettyServerHandler(this.applicationContext!!))
        return nettyWebServerFactory
    }

    @Bean
    fun requestMappingHandlerMapping(): RequestMappingHandlerMapping {
        return RequestMappingHandlerMapping()
    }

    @Bean
    fun requestMappingHandlerAdapter(): RequestMappingHandlerAdapter {
        val handlerAdapter = RequestMappingHandlerAdapter()
        handlerAdapter.setHttpMessageConverters(arrayListOf(MappingJackson2HttpMessageConverter()))
        return handlerAdapter
    }

    @Bean
    fun dispatcherHandler(): DispatcherHandler {
        val dispatcherHandler = DispatcherHandlerImpl()
        dispatcherHandler.setApplicationContext(this.applicationContext!!)
        return dispatcherHandler
    }
}