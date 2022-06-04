package com.wanna.framework.web.config.annotation

import com.wanna.framework.context.format.FormatterRegistry
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler


/**
 * WebMvcConfigurer，支持去对SpringMebMvc当中的各个组件去进行自定义
 *
 * @see WebMvcConfigurationSupport
 * @see DelegatingWebMvcConfiguration
 */
interface WebMvcConfigurer {

    /**
     * 自定义内容协商策略
     *
     * @param configurer 内容协商的Configurer
     */
    fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {}

    /**
     * 添加拦截器列表，往给定的拦截器注册中心当中去添加拦截器，即可实现SpringWebMvc从拦截器的添加
     *
     * @param registry 拦截器的注册中心
     */
    fun addInterceptors(registry: InterceptorRegistry) {}

    /**
     * 添加一个HandlerMethod的参数解析器
     *
     * @param resolvers SpringWebMvc当中的参数解析器列表
     */
    fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {}

    /**
     * 添加一个返回值处理器
     *
     * @param handlers SpringWebMvc当中的返回值处理器列表
     */
    fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {}

    /**
     * 自定义MessageConverter(替换掉默认的MessageConverter列表)
     *
     * @param converters SpringWevMvc的MessageConverter列表
     */
    fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {}

    /**
     * 扩展MessageConverter(在默认的MessageConverter列表的情况下，新增MessageConverter)
     *
     * @param converters SpringWebMvc的MessageConverters列表
     */
    fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {}

    /**
     * 自定义HandlerExceptionResolver(替换掉默认的HandlerExceptionResolver的列表)
     *
     * @param resolvers SpringWebMvc的HandlerExceptionResolver列表
     */
    fun configureHandlerExceptionResolvers(resolvers: MutableList<HandlerExceptionResolver>) {}

    /**
     * 扩展HandlerExceptionResolver(在默认的HandlerExceptionResolver的列表的基础上去进行罗站)
     *
     * @param resolvers SpringWebMvc的HandlerExceptionResolver列表
     */
    fun extendHandlerExceptionResolvers(resolvers: MutableList<HandlerExceptionResolver>) {}

    /**
     * 添加Formatter(Converter)，提供SpringWebMvc当中类型的转换支持
     *
     * @param formatterRegistry SpringWebMvc的Formatter注册中心
     */
    fun addFormatters(formatterRegistry: FormatterRegistry) {}
}