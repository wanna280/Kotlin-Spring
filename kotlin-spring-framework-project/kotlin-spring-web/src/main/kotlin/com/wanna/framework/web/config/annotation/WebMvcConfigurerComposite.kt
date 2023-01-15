package com.wanna.framework.web.config.annotation

import com.wanna.framework.context.format.FormatterRegistry
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler

/**
 * 它聚合了WebMvcConfigurer, 去进行委托完成WebMvc的配置
 */
open class WebMvcConfigurerComposite : WebMvcConfigurer {

    private val webMvcConfigurers = ArrayList<WebMvcConfigurer>()

    open fun addWebMvcConfigurers(configures: Collection<WebMvcConfigurer>) {
        this.webMvcConfigurers += configures
    }

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {

    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        this.webMvcConfigurers.forEach { it.addInterceptors(registry) }
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        this.webMvcConfigurers.forEach { it.addArgumentResolvers(resolvers) }
    }

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        this.webMvcConfigurers.forEach { it.addReturnValueHandlers(handlers) }
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        this.webMvcConfigurers.forEach { it.configureMessageConverters(converters) }
    }

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        this.webMvcConfigurers.forEach { it.extendMessageConverters(converters) }
    }

    override fun configureHandlerExceptionResolvers(resolvers: MutableList<HandlerExceptionResolver>) {
        this.webMvcConfigurers.forEach { it.configureHandlerExceptionResolvers(resolvers) }
    }

    override fun extendHandlerExceptionResolvers(resolvers: MutableList<HandlerExceptionResolver>) {
        this.webMvcConfigurers.forEach { it.extendHandlerExceptionResolvers(resolvers) }
    }

    override fun addFormatters(formatterRegistry: FormatterRegistry) {
        this.webMvcConfigurers.forEach { it.addFormatters(formatterRegistry) }
    }

    override fun addCorsMapping(registry: CorsRegistry) {
        this.webMvcConfigurers.forEach { it.addCorsMapping(registry) }
    }
}