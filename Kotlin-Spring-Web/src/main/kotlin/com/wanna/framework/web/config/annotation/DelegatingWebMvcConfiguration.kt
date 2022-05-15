package com.wanna.framework.web.config.annotation

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler

/**
 * 它是一个为WebMvcConfiguration提供扩展的配置类，它在提供父类的当中的webMvc的相关支持的前提下；
 * 支持使用WebMvcConfigurer去对WebMvc当中的各个组件去进行扩展和自定义
 */
@Configuration(proxyBeanMethods = false)
open class DelegatingWebMvcConfiguration : WebMvcConfigurationSupport() {

    private val webMvcConfigurerComposite = WebMvcConfigurerComposite()

    /**
     * 自动注入所有的WebMvcConfigurer，去对WebMvc去进行扩展
     */
    @Autowired(required = false)
    open fun setWebMvcConfigurers(configurers: List<WebMvcConfigurer>) {
        this.webMvcConfigurerComposite.addWebMvcConfigurers(configurers)
    }

    override fun configureContentNegotiation(contentNegotiationConfigurer: ContentNegotiationConfigurer) {
        this.webMvcConfigurerComposite.configureContentNegotiation(contentNegotiationConfigurer)
    }

    override fun extendsArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        this.webMvcConfigurerComposite.addArgumentResolvers(resolvers)
    }

    override fun extendsReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        this.webMvcConfigurerComposite.addReturnValueHandlers(handlers)
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        this.webMvcConfigurerComposite.configureMessageConverters(converters)
    }

    override fun extendsMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        this.webMvcConfigurerComposite.extendMessageConverters(converters)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        this.webMvcConfigurerComposite.addInterceptors(registry)
    }

    override fun configureHandlerExceptionResolver(resolvers: MutableList<HandlerExceptionResolver>) {
        this.webMvcConfigurerComposite.configureHandlerExceptionResolvers(resolvers)
    }

    override fun extendsHandlerExceptionResolver(resolvers: MutableList<HandlerExceptionResolver>) {
        this.webMvcConfigurerComposite.extendHandlerExceptionResolvers(resolvers)
    }
}