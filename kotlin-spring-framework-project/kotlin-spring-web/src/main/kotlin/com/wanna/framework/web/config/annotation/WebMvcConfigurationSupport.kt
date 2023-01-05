package com.wanna.framework.web.config.annotation

import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.format.FormatterRegistry
import com.wanna.framework.context.format.support.DefaultFormattingConversionService
import com.wanna.framework.context.format.support.FormattingConversionService
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.validation.Errors
import com.wanna.framework.validation.Validator
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.HttpRequestHandler
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.cors.CorsConfiguration
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.SimpleUrlHandlerMapping
import com.wanna.framework.web.handler.ViewResolver
import com.wanna.framework.web.http.converter.ByteArrayHttpMessageConverter
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.http.converter.StringHttpMessageConverter
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
import com.wanna.framework.web.mvc.Controller
import com.wanna.framework.web.mvc.HttpRequestHandlerAdapter
import com.wanna.framework.web.mvc.SimpleControllerHandlerAdapter
import com.wanna.framework.web.mvc.annotation.ResponseStatusExceptionResolver
import com.wanna.framework.web.mvc.support.DefaultHandlerExceptionResolver

/**
 * 为WebMvc提供支持的配置类，它为WebMvc的正常运行提供的一些默认的相关组件，并配置到容器当中...
 * 在它的子类DelegatingWebMvcConfiguration当中，基于这个类当中的一些模板方法，支持使用WebMvcConfigurer去对相关的组件去进行自定义工作；
 * 比如自定义参数解析器、返回值处理器、MessageConverter、内容协商管理器等组件去进行配置/扩展
 *
 * @see DelegatingWebMvcConfiguration
 */
open class WebMvcConfigurationSupport : ApplicationContextAware {

    companion object {

        /**
         * Jackson是否存在的标识?
         */
        @JvmStatic
        private val jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper")
    }

    /**
     * ApplicationContext
     */
    @Nullable
    private var applicationContext: ApplicationContext? = null

    /**
     * 要去注册给SpringMVC的拦截器列表
     */
    @Nullable
    private var interceptors: MutableList<Any>? = null

    /**
     * 提供SpringMVC的HandlerMethod的参数解析的参数解析器
     */
    @Nullable
    private var argumentResolvers: MutableList<HandlerMethodArgumentResolver>? = null

    /**
     * 提供SpringMVC的HandlerMethod的返回解析的返回解析器
     */
    @Nullable
    private var returnValueHandlers: MutableList<HandlerMethodReturnValueHandler>? = null

    /**
     * 内容协商管理器, 负责去解析用户的请求想要返回什么格式的报文
     */
    @Nullable
    private var contentNegotiationManager: ContentNegotiationManager? = null

    /**
     * 提供对于消息转换的MessageConverter
     */
    @Nullable
    private var messageConverters: MutableList<HttpMessageConverter<*>>? = null

    /**
     * CORS跨域的配置信息, Key-PathPattern, Value-该Pattern下的CORS配置信息
     */
    @Nullable
    private var corsConfigurations: Map<String, CorsConfiguration>? = null

    /**
     * 设置ApplicationContext
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 为SpringMVC去注册一个处理RequestMapping的请求的HandlerMapping
     *
     * @param contentNegotiationManager SpringMVC需要用到的内容协商管理器
     * @param conversionService SpringMVC需要使用到的ConversionService
     * @return RequestMappingHandlerMapping
     */
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
    open fun mvcContentNegotiationManager(): ContentNegotiationManager {
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
     * Note: 整个SpringMVC当中的各个组件，都将会采用这个ConversionService去完成类型的转换工作
     */
    @Bean
    @Qualifier("mvcConversionService")
    open fun mvcConversionService(): FormattingConversionService {
        val formattingConversionService = DefaultFormattingConversionService()
        // 扩展ConversionService当中的Formatter
        addFormatters(formattingConversionService)
        return formattingConversionService
    }

    /**
     * 提供SpringMVC当中的异常解解析的HandlerExceptionResolver
     *
     * @param contentNegotiationManager ContentNegotiationManager
     * @return HandlerExceptionResolver
     * @see ExceptionHandlerExceptionResolver
     */
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
     * 给容器中注册一个BeanName的ViewResolver, 基于BeanName去找到对应的View视图对象, 从而进行视图的渲染
     *
     * @return ViewResolver
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

    /**
     * HttpRequestHandler的HandlerAdapter, 负责基于HttpRequestHandler的方式去进行请求的处理
     *
     * @see HttpRequestHandler
     * @return Handler for HttpRequestHandler
     */
    @Bean("httpRequestHandlerAdapter")
    @Qualifier("httpRequestHandlerAdapter")
    open fun httpRequestHandlerAdapter(): HttpRequestHandlerAdapter {
        return HttpRequestHandlerAdapter()
    }

    /**
     * 利用Controller去处理请求的HandlerAdapter
     *
     * @return HandlerAdapter for Controller
     * @see Controller
     */
    @Bean("simpleControllerHandlerAdapter")
    @Qualifier("simpleControllerHandlerAdapter")
    open fun simpleControllerHandlerAdapter(): SimpleControllerHandlerAdapter {
        return SimpleControllerHandlerAdapter()
    }

    /**
     * 为SpringMVC导入一个Spring的Validator，为`@ModelAttribute`和`@RequestBody`的参数检验提供支持
     *
     * @return 提供SpringMVC的参数检验的Validator
     */
    @Bean("mvcValidator")
    @Qualifier("mvcValidator")
    open fun mvcValidator(): Validator {
        // 1.首先尝试获取子类去进行自定义的Validator
        var validator = getValidator()

        // 2.如果没有自定义Validator的话，那么我们直接去进行推断...
        if (validator == null) {

            // 检查JDK的Validator是否在我们的依赖当中？
            if (ClassUtils.isPresent(
                    "javax.validation.Validator", WebMvcConfigurationSupport::class.java.classLoader
                )
            ) {
                // 实例化出来一个OptionalValidatorFactoryBean对象，支持去探测本地的javax.validation.Validator作为delegate
                val className = "com.wanna.framework.validation.beanvalidation.OptionalValidatorFactoryBean"
                try {
                    val clazz =
                        ClassUtils.forName<Validator>(className, WebMvcConfigurationSupport::class.java.classLoader)
                    validator = BeanUtils.instantiateClass(clazz)
                    // 如果该类找不到的话...那么直接丢出来异常
                } catch (ex: ClassNotFoundException) {
                    throw IllegalStateException(ex)
                } catch (ex: LinkageError) {
                    throw IllegalStateException(ex)
                }
                // 如果依赖当中都没有javax.validation.Validator，那么说明没有合适的Validator可以去进行使用
                // 我们直接尝试去使用NoOpValidator...
            } else {
                validator = NoOpValidator()
            }
        }
        return validator
    }

    /**
     * 交给子类去进行重写，提供自定义的Validator
     *
     * @return 你需要使用的Validator
     */
    @Nullable
    protected open fun getValidator(): Validator? {
        return null
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
     *
     * @param converters MessageConverter列表
     */
    protected open fun applyDefaultMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters += ByteArrayHttpMessageConverter()  // support ByteArray
        converters += StringHttpMessageConverter()  // support String


        if (jackson2Present) {
            converters += MappingJackson2HttpMessageConverter()  // support Json
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
     * 应用默认的异常解析器
     * * 1.添加一个处理`@ExceptionHandler`注解的[ExceptionHandlerExceptionResolver]
     * * 2.添加一个处理`@ResponseStatus`注解的[ResponseStatusExceptionResolver]
     * * 3.添加一个处理SpringMVC的异常的[DefaultHandlerExceptionResolver]成为HTTP的错误状态码
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
        if (this.applicationContext != null) {
            exceptionHandlerExceptionResolver.setApplicationContext(this.applicationContext!!)
        }
        exceptionHandlerExceptionResolver.afterPropertiesSet()

        // 添加一个处理@ExceptionHandler注解的HandlerExceptionResolver
        resolvers += exceptionHandlerExceptionResolver

        // 添加一个处理@ResponseStatus的HandleExceptionResolver
        resolvers += ResponseStatusExceptionResolver()

        // 添加一个默认的HandlerExceptionResolver处理SpringMVC当中抛出来的异常信息去成为HTTP的错误状态码
        resolvers += DefaultHandlerExceptionResolver()
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

    /**
     * 啥操作也不做的Validator
     */
    private class NoOpValidator : Validator {
        override fun supports(clazz: Class<*>) = false

        override fun validate(target: Any, errors: Errors) {}
    }
}