package com.wanna.framework.web.config.annotation

import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.format.FormatterRegistry
import com.wanna.framework.context.format.support.DefaultFormattingConversionService
import com.wanna.framework.context.format.support.FormattingConversionService
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.cors.CorsConfiguration
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.SimpleUrlHandlerMapping
import com.wanna.framework.web.handler.ViewResolver
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.http.converter.json.MappingJackson2HttpMessageConverter
import com.wanna.framework.web.method.DefaultRequestToViewNameTranslator
import com.wanna.framework.web.method.RequestToViewNameTranslator
import com.wanna.framework.web.method.annotation.ExceptionHandlerExceptionResolver
import com.wanna.framework.web.method.annotation.RequestMappingHandlerAdapter
import com.wanna.framework.web.method.annotation.RequestMappingHandlerMapping
import com.wanna.framework.web.method.support.HandlerExceptionResolverComposite
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.method.view.BeanNameViewResolver
import com.wanna.framework.web.method.view.TemplateViewResolver
import com.wanna.framework.web.mvc.HttpRequestHandlerAdapter
import com.wanna.framework.web.mvc.SimpleControllerHandlerAdapter

/**
 * 为WebMvc提供支持的配置类，它为WebMvc的正常运行提供的一些默认的相关组件，并配置到容器当中...
 * 在它的子类DelegatingWebMvcConfiguration当中，基于这个类当中的一些模板方法，支持使用WebMvcConfigurer去对相关的组件去进行自定义工作；
 * 比如自定义参数解析器、返回值处理器、MessageConverter、内容协商管理器等组件去进行配置/扩展
 *
 * @see DelegatingWebMvcConfiguration
 */
open class WebMvcConfigurationSupport : ApplicationContextAware {

    companion object {
        private val jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper")
    }

    private lateinit var applicationContext: ApplicationContext

    private var interceptors: MutableList<Any>? = null

    private var argumentResolvers: MutableList<HandlerMethodArgumentResolver>? = null

    private var returnValueHandlers: MutableList<HandlerMethodReturnValueHandler>? = null

    private var contentNegotiationManager: ContentNegotiationManager? = null

    private var messageConverters: MutableList<HttpMessageConverter<*>>? = null

    private var corsConfigurations: Map<String, CorsConfiguration>? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    @Bean
    @Qualifier("requestMappingHandlerMapping")
    open fun requestMappingHandlerMapping(
        @Qualifier("mvcContentNegotiationManager") contentNegotiationManager: ContentNegotiationManager,
        @Qualifier("mvcConversionService") conversionService: FormattingConversionService
    ): RequestMappingHandlerMapping {
        val mapping = createRequestMappingHandlerMapping()
        // 设置Interceptors列表
        mapping.setInterceptors(*getInterceptors(conversionService))
        mapping.setCorsConfigurations(getCorsConfigurations())
        return mapping
    }

    @Bean
    @Qualifier("mvcContentNegotiationManager")
    open fun contentNegotiationManager(): ContentNegotiationManager {
        var negotiationManager = this.contentNegotiationManager
        if (negotiationManager == null) {
            val contentNegotiationConfigurer = ContentNegotiationConfigurer()
            negotiationManager = contentNegotiationConfigurer.build()
            this.contentNegotiationManager = negotiationManager
        }
        return negotiationManager
    }

    @Bean
    @Qualifier("requestMappingHandlerAdapter")
    open fun requestMappingHandlerAdapter(
        @Qualifier("mvcContentNegotiationManager") contentNegotiationManager: ContentNegotiationManager,
        @Qualifier("mvcConversionService") conversionService: FormattingConversionService
    ): RequestMappingHandlerAdapter {
        val handlerAdapter = createRequestMappingHandlerAdapter()
        handlerAdapter.setHttpMessageConverters(getMessageConverters())
        handlerAdapter.setContentNegotiationManager(contentNegotiationManager)

        // 设置自定义的参数解析器，不会替换默认的，沿用默认的并进行扩展
        handlerAdapter.setCustomArgumentResolvers(getArgumentResolvers())

        // 设置定义的返回值解析器，不会替换默认的，沿用默认的并扩展
        handlerAdapter.setCustomReturnValueHandlers(getReturnValueResolvers())

        return handlerAdapter
    }

    /**
     * 给容器当中去注册一个ConversionService，去支持进行WebMvc当中的类型转换工作
     *
     * Note: 整个SpringMvc当中的各个组件，都将会采用这个ConversionService去完成类型的转换工作
     */
    @Bean
    @Qualifier("mvcConversionService")
    open fun formattingConversionService(): FormattingConversionService {
        val formattingConversionService = DefaultFormattingConversionService()
        // 扩展ConversionService当中的Formatter
        addFormatters(formattingConversionService)
        return formattingConversionService
    }

    @Bean
    @Qualifier("handlerExceptionResolver")
    open fun handlerExceptionResolver(@Qualifier("mvcContentNegotiationManager") contentNegotiationManager: ContentNegotiationManager): HandlerExceptionResolver {
        val exceptionResolvers = ArrayList<HandlerExceptionResolver>()
        configureHandlerExceptionResolver(exceptionResolvers)  // configure
        if (exceptionResolvers.isEmpty()) {
            applyDefaultHandlerExceptionResolver(exceptionResolvers, contentNegotiationManager)  // applyDefault
        }
        extendsHandlerExceptionResolver(exceptionResolvers)  // extends

        // 创建一个HandlerExceptionResolverComposite，把全部的异常解析器全部去进行包装
        val composite = HandlerExceptionResolverComposite()
        composite.setOrder(0)
        composite.setHandlerExceptionResolver(exceptionResolvers)
        return composite
    }

    /**
     * 给容器中注册一个BeanName的ViewResolver
     */
    @Bean
    @Qualifier("beanNameViewResolver")
    open fun beanNameViewResolver(): ViewResolver {
        return BeanNameViewResolver()
    }

    @Bean
    @Qualifier("templateViewResolver")
    open fun templateViewResolver(): ViewResolver {
        return TemplateViewResolver()
    }

    @Bean("viewNameTranslator")
    @Qualifier("viewNameTranslator")
    open fun viewNameTranslator(): RequestToViewNameTranslator {
        return DefaultRequestToViewNameTranslator()
    }

    @Bean("urlHandlerMapping")
    @Qualifier("urlHandlerMapping")
    open fun urlHandlerMapping(): HandlerMapping {
        val urlHandlerMapping = SimpleUrlHandlerMapping()
        urlHandlerMapping.setCorsConfigurations(getCorsConfigurations())
        return urlHandlerMapping
    }

    @Bean("httpRequestHandlerAdapter")
    @Qualifier("httpRequestHandlerAdapter")
    open fun httpRequestHandlerAdapter(): HttpRequestHandlerAdapter {
        return HttpRequestHandlerAdapter()
    }

    @Bean("simpleControllerHandlerAdapter")
    @Qualifier("simpleControllerHandlerAdapter")
    open fun simpleControllerHandlerAdapter(): SimpleControllerHandlerAdapter {
        return SimpleControllerHandlerAdapter()
    }

    protected open fun getArgumentResolvers(): List<HandlerMethodArgumentResolver> {
        var argumentResolvers = this.argumentResolvers
        if (argumentResolvers == null) {
            argumentResolvers = ArrayList()
            extendsArgumentResolvers(argumentResolvers)
            this.argumentResolvers = argumentResolvers
        }
        return argumentResolvers
    }

    protected open fun getReturnValueResolvers(): List<HandlerMethodReturnValueHandler> {
        var handlers = this.returnValueHandlers
        if (handlers == null) {
            handlers = ArrayList()
            extendsReturnValueHandlers(handlers)
            this.returnValueHandlers = handlers
        }
        return handlers
    }


    /**
     * 获取应该要去进行应用的MessageConverter列表；
     * 1.交给用户去自定义MessageConverter列表，如果你没有应用，那么我给你应用默认的；如果你有了，就不使用默认的了！
     * 2.交给用户去自定义的扩展MessageConverter列表
     *
     * Note: configure是直接替换默认的，extends是在默认的基础上去进行扩展
     */
    protected open fun getMessageConverters(): MutableList<HttpMessageConverter<*>> {
        var messageConverters = this.messageConverters
        if (messageConverters == null) {
            messageConverters = ArrayList()
            configureMessageConverters(messageConverters)  // configure
            if (messageConverters.isEmpty()) {
                applyDefaultMessageConverters(messageConverters)  // apply default
            }
            extendsMessageConverters(messageConverters)  // extends
        }
        return messageConverters
    }

    /**
     * 应用默认的MessageConverter列表
     */
    protected open fun applyDefaultMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        if (jackson2Present) {
            converters += MappingJackson2HttpMessageConverter()
        }
    }

    protected open fun createRequestMappingHandlerAdapter(): RequestMappingHandlerAdapter {
        return RequestMappingHandlerAdapter()
    }

    protected open fun createRequestMappingHandlerMapping(): RequestMappingHandlerMapping {
        return RequestMappingHandlerMapping()
    }

    /**
     * 获取Interceptor列表，交给子类去进行扩展
     */
    protected open fun getInterceptors(conversionService: FormattingConversionService): Array<Any> {
        var interceptors = this.interceptors
        if (interceptors == null) {
            val registry = InterceptorRegistry()
            // 模板方法，交给子类去进行扩展
            addInterceptors(registry)
            interceptors = ArrayList(registry.getInterceptors())
            this.interceptors = interceptors
        }
        return interceptors.toTypedArray()
    }

    /**
     * 获取Cors的配置信息，它将会apply给所有的HandlerMapping当中
     *
     * @return Cors配置信息，key-pathPattern，value-CorsConfiguration
     */
    protected fun getCorsConfigurations(): Map<String, CorsConfiguration> {
        var corsConfigurations = this.corsConfigurations
        if (corsConfigurations == null) {
            val corsRegistry = CorsRegistry()
            addCorsMapping(corsRegistry)
            corsConfigurations = corsRegistry.getCorsConfigurations()
        }
        return corsConfigurations
    }


    /**
     * 应用默认的异常解析器，添加一个处理@ExceptionHandler的ExceptionResolver
     *
     * @param contentNegotiationManager 内容协商管理器
     * @param resolvers 支持去进行扩展的HandlerExceptionResolver
     */
    protected open fun applyDefaultHandlerExceptionResolver(
        resolvers: MutableList<HandlerExceptionResolver>, contentNegotiationManager: ContentNegotiationManager
    ) {
        // 创建一个ExceptionHandler的ExceptionResolver(它需要的组件，和HandlerAdapter完全类似)
        // 并配置内容协商管理器，参数解析器、返回值处理器、消息转换器
        val exceptionHandlerExceptionResolver = ExceptionHandlerExceptionResolver()
        exceptionHandlerExceptionResolver.setContentNegotiationManager(contentNegotiationManager)
        exceptionHandlerExceptionResolver.setHandlerMethodArgumentResolvers(getArgumentResolvers())
        exceptionHandlerExceptionResolver.setHttpMessageConverters(getMessageConverters())
        exceptionHandlerExceptionResolver.setHandlerMethodReturnValueHandlers(getReturnValueResolvers())


        // 手动设置ApplicationContext，并完成初始化工作...
        // 因为它不是一个SpringBean，无法自动初始化...我们尝试去进行手动初始化
        exceptionHandlerExceptionResolver.setApplicationContext(this.applicationContext)
        exceptionHandlerExceptionResolver.afterPropertiesSet()

        resolvers += exceptionHandlerExceptionResolver
    }

    /**
     * 自定义的添加Interceptor的逻辑，模板方法，交给子类去进行实现
     *
     * @param registry 拦截器的注册中心，可以通过往其中添加拦截器实现拦截器的注册
     */
    protected open fun addInterceptors(registry: InterceptorRegistry) {}

    /**
     * 自定义默认的默认的异常解析器(模板方法，交给子类去进行实现)
     */
    protected open fun configureHandlerExceptionResolver(resolvers: MutableList<HandlerExceptionResolver>) {}

    /**
     * 扩展自定义的异常解析器(模板方法，交给子类去进行实现)
     */
    protected open fun extendsHandlerExceptionResolver(resolvers: MutableList<HandlerExceptionResolver>) {}

    protected open fun addFormatters(formatterRegistry: FormatterRegistry) {}

    /**
     * 自定义MessageConverter列表，交给子类去进行自定义
     *
     * @param converters 将要应用的MessageConverter列表
     */
    protected open fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {}

    /**
     * 扩展MessageConverter列表，交给子类去扩展
     *
     * @param converters 将要应用的MessageConverter列表
     */
    protected open fun extendsMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {}


    /**
     * 扩展参数解析器，交给子类去进行扩展(模板方法)
     */
    protected open fun extendsArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {}

    /**
     * 扩展返回值处理器，交给子类去进行扩展(模板方法)
     */
    protected open fun extendsReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {}

    /**
     * 自定义内容协商策略，交给子类去进行扩展(模板方法)
     */
    protected open fun configureContentNegotiation(contentNegotiationConfigurer: ContentNegotiationConfigurer) {}

    /**
     * 交给子类去扩展CorsMapping，可以自行往其中去添加Cors的配置信息
     *
     * @param registry CorsRegistry
     */
    protected open fun addCorsMapping(registry: CorsRegistry) {}
}