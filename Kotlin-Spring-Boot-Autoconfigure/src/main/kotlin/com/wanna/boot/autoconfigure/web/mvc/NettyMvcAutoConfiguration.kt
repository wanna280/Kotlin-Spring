package com.wanna.boot.autoconfigure.web.mvc

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnWebApplication
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.boot.web.mvc.server.WebServerFactory
import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Primary
import com.wanna.framework.context.format.support.FormattingConversionService
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.DispatcherHandlerImpl
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.config.annotation.DelegatingWebMvcConfiguration
import com.wanna.framework.web.method.annotation.RequestMappingHandlerMapping
import com.wanna.framework.web.server.netty.server.support.NettyServerHandler

/**
 * 只有在WebMvc下才生效的自动配置类
 */
@ConditionalOnWebApplication(ConditionalOnWebApplication.Type.MVC)
@EnableConfigurationProperties([NettyWebServerProperties::class])  // 导入WebServerProperties配置文件
@Configuration(proxyBeanMethods = false)
open class NettyMvcAutoConfiguration : ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    @Bean
    @ConditionalOnMissingBean
    open fun dispatcherHandler(): DispatcherHandler {
        val dispatcherHandler = DispatcherHandlerImpl()
        dispatcherHandler.setApplicationContext(this.applicationContext)
        return dispatcherHandler
    }

    /**
     * 给SpringBeanFactory当中导入一个NettyWebServerFactory
     * 它的作用是，为Spring的ApplicationContext去提供WebServer，并完成启动
     *
     * @param properties WebServer的配置文件
     */
    @Bean
    open fun nettyWebServerFactory(properties: NettyWebServerProperties): WebServerFactory {
        val nettyWebServerFactory = NettyWebServerFactory()
        nettyWebServerFactory.setHandler(NettyServerHandler(this.applicationContext))
        nettyWebServerFactory.setPort(properties.port)
        return nettyWebServerFactory
    }

    /**
     * 开启SpringMVC的的配置的类，导入SpringMVC需要用到的各个相关的组件
     */
    @Configuration(proxyBeanMethods = false)
    open class EnableWebMvcConfiguration : DelegatingWebMvcConfiguration() {

        @Primary  // set to Primary
        @Bean
        @Qualifier("requestMappingHandlerMapping")
        override fun requestMappingHandlerMapping(
            @Qualifier("mvcContentNegotiationManager") contentNegotiationManager: ContentNegotiationManager,
            @Qualifier("mvcConversionService") conversionService: FormattingConversionService
        ): RequestMappingHandlerMapping {
            return super.requestMappingHandlerMapping(contentNegotiationManager, conversionService)
        }
    }
}