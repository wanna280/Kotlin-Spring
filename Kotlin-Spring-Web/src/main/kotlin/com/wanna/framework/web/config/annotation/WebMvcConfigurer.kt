package com.wanna.framework.web.config.annotation

import com.wanna.framework.context.format.FormatterRegistry
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler

interface WebMvcConfigurer {

    fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {}

    fun addInterceptors(registry: InterceptorRegistry) {}

    fun addArgumentResolvers(resolvers: List<HandlerMethodArgumentResolver>) {}

    fun addReturnValueHandlers(handlers: List<HandlerMethodReturnValueHandler>) {}

    fun configureMessageConverters(converters: List<HttpMessageConverter<*>>) {}

    fun extendMessageConverters(converters: List<HttpMessageConverter<*>>) {}

    fun configureHandlerExceptionResolvers(resolvers: List<HandlerExceptionResolver>) {}

    fun extendHandlerExceptionResolvers(resolvers: List<HandlerExceptionResolver?>) {}

    fun addFormatters(formatterRegistry: FormatterRegistry) {}
}