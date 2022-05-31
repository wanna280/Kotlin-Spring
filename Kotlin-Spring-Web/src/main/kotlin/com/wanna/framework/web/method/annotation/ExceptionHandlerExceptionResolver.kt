package com.wanna.framework.web.method.annotation

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.ControllerAdviceBean
import com.wanna.framework.web.method.support.*
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个@ExceptionHandler的HandlerExceptionResolver，采用@ExceptionHandler的方式去处理处理请求当中发生的异常；
 * 支持从@ControllerAdvice的Bean上去寻找@ExceptionHandler注解标注的方法
 *
 * @see HandlerExceptionResolver
 */
open class ExceptionHandlerExceptionResolver : HandlerExceptionResolver, ApplicationContextAware, InitializingBean {

    private var messageConverters: List<HttpMessageConverter<*>>? = null

    private var argumentResolvers: List<HandlerMethodArgumentResolver>? = null

    private var returnValueHandlers: List<HandlerMethodReturnValueHandler>? = null

    private var contentNegotiationManager: ContentNegotiationManager = ContentNegotiationManager()

    private var applicationContext: ApplicationContext? = null

    // ExceptionHandler的缓存
    private val exceptionHandlerCache = ConcurrentHashMap<Class<*>, ExceptionHandlerMethodResolver>(64)

    // ExceptionHandlerAdviceMap(key-ControllerAdviceBean，value-ExceptionHandlerMethodResolver)
    private val exceptionHandlerAdviceCache =
        ConcurrentHashMap<ControllerAdviceBean, ExceptionHandlerMethodResolver>(64)

    override fun resolveException(
        request: HttpServerRequest, response: HttpServerResponse, handler: Any?, ex: Throwable
    ): ModelAndView? {

        var exceptionHandlerMethod: InvocableHandlerMethod? = null
        exceptionHandlerAdviceCache.forEach { (bean, resolver) ->
            val resolveMethod = resolver.resolveMethod(ex)
            if (resolveMethod != null) {
                exceptionHandlerMethod =
                    InvocableHandlerMethod.newInvocableHandlerMethod(bean.resolveBean(), resolveMethod)
            }
        }
        if (exceptionHandlerMethod == null) {
            response.sendError(500)
            return null
        }
        val invocableHandlerMethod = exceptionHandlerMethod!!
        if (this.argumentResolvers != null) {
            if (this.argumentResolvers!!.isEmpty()) {
                this.argumentResolvers = ArrayList(getDefaultArgumentResolvers())
            }
            val composite = HandlerMethodArgumentResolverComposite()
            composite.addArgumentResolvers(this.argumentResolvers!!)
            invocableHandlerMethod.argumentResolvers = composite
        }
        if (this.returnValueHandlers != null) {
            if (this.returnValueHandlers!!.isEmpty()) {
                this.returnValueHandlers = ArrayList(getDefaultReturnValueHandlers())
            }
            val composite = HandlerMethodReturnValueHandlerComposite()
            composite.addReturnValueHandlers(this.returnValueHandlers!!)
            invocableHandlerMethod.returnValueHandlers = composite
        }

        val mavContainer = ModelAndViewContainer()

        val webRequest = ServerWebRequest(request, response)

        // 执行目标ExceptionHandler方法...
        invocableHandlerMethod.invokeAndHandle(webRequest,mavContainer)

        // return an empty ModelAndView to pass next HandlerExceptionResolvers and not to render view...
        return ModelAndView()
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun afterPropertiesSet() {
        // 初始化ExceptionHandler
        initExceptionHandlerAdviceCache()
    }

    /**
     * 从SpringBeanFactory当中找到所有的@ControllerAdvice的Bean，并找出所有的@ExceptionHandler方法
     */
    private fun initExceptionHandlerAdviceCache() {
        val controllerAdviceBeans = ControllerAdviceBean.findAnnotatedBeans(this.applicationContext!!)
        controllerAdviceBeans.forEach {
            val handlerMethodResolver = ExceptionHandlerMethodResolver(it.getBeanType())
            if (handlerMethodResolver.hasExceptionMappings()) {
                exceptionHandlerAdviceCache[it] = handlerMethodResolver
            }
        }
    }

    /**
     * 如果不进行自定义，那么需要去获取默认的HandlerMethod的参数解析器列表
     */
    private fun getDefaultArgumentResolvers(): List<HandlerMethodArgumentResolver> {
        val resolvers = ArrayList<HandlerMethodArgumentResolver>()

        // 添加@RequestParam、@RequestHeader的参数解析器
        resolvers += RequestHeaderMethodArgumentResolver()
        resolvers += RequestParamMethodArgumentResolver()

        // 处理ServerRequest和ServerResponse的参数处理器
        resolvers += ServerRequestMethodArgumentResolver()
        resolvers += ServerResponseMethodArgumentResolver()

        // 添加RequestResponseBody的方法处理器
        resolvers += RequestResponseBodyMethodProcessor(getHttpMessageConverters(), getContentNegotiationManager())

        return resolvers
    }

    /**
     * 如果不进行自定义，那么需要去获取默认的ReturnValueHandlers列表
     */
    private fun getDefaultReturnValueHandlers(): List<HandlerMethodReturnValueHandler> {
        val handlers = ArrayList<HandlerMethodReturnValueHandler>()
        handlers += RequestResponseBodyMethodProcessor(getHttpMessageConverters(), getContentNegotiationManager())

        return handlers
    }

    open fun getContentNegotiationManager(): ContentNegotiationManager {
        return this.contentNegotiationManager
    }

    open fun setContentNegotiationManager(contentNegotiationManager: ContentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager
    }

    open fun setHttpMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        this.messageConverters = converters
    }

    open fun setHandlerMethodArgumentResolvers(resolvers: List<HandlerMethodArgumentResolver>) {
        this.argumentResolvers = resolvers
    }

    open fun getHttpMessageConverters(): List<HttpMessageConverter<*>> {
        if (this.messageConverters == null) {
            throw IllegalStateException("请先初始化RequestMappingHandlerAdapter的MessageConverter列表")
        }
        return this.messageConverters!!
    }

    open fun setHandlerMethodReturnValueHandlers(handlers: List<HandlerMethodReturnValueHandler>) {
        this.returnValueHandlers = handlers
    }
}