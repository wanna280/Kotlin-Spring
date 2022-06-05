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
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个@ExceptionHandler的HandlerExceptionResolver，采用@ExceptionHandler的方式去处理处理请求当中发生的异常；
 *
 * * 1.支持从@ControllerAdvice的Bean上去寻找@ExceptionHandler注解标注的方法
 * * 2.支持从Controller的Bean上去寻找@ExceptionHandler
 *
 * @see HandlerExceptionResolver
 */
open class ExceptionHandlerExceptionResolver : HandlerExceptionResolver, ApplicationContextAware, InitializingBean {

    companion object {
        private val logger = LoggerFactory.getLogger(ExceptionHandlerMethodResolver::class.java)
    }

    private var messageConverters: List<HttpMessageConverter<*>>? = null

    private var argumentResolvers: List<HandlerMethodArgumentResolver>? = null

    // 交给外部去进行自定义的参数解析器(基于默认的去进行扩展)
    private var customArgumentResolvers: List<HandlerMethodArgumentResolver>? = null

    // 交给外部去进行自定义的返回值处理器(基于默认的去进行扩展)
    private var customReturnValueHandlers: List<HandlerMethodReturnValueHandler>? = null

    private var returnValueHandlers: List<HandlerMethodReturnValueHandler>? = null

    private var contentNegotiationManager: ContentNegotiationManager = ContentNegotiationManager()

    private var applicationContext: ApplicationContext? = null

    // ExceptionHandler的缓存，key-handlerType(Controller)，value-ExceptionHandlerMethodResolver
    private val exceptionHandlerCache = ConcurrentHashMap<Class<*>, ExceptionHandlerMethodResolver>(64)

    // ExceptionHandlerAdviceMap(key-ControllerAdviceBean，value-ExceptionHandlerMethodResolver)
    private val exceptionHandlerAdviceCache =
        ConcurrentHashMap<ControllerAdviceBean, ExceptionHandlerMethodResolver>(64)

    override fun resolveException(
        request: HttpServerRequest, response: HttpServerResponse, handler: Any?, ex: Throwable
    ): ModelAndView? {

        // 遍历所有的ControllerAdvice，去寻找合适的ExceptionHandler去处理异常
        val exceptionHandlerMethod = getExceptionHandlerMethod(handler as InvocableHandlerMethod, ex)
        if (exceptionHandlerMethod == null) {
            response.sendError(500) // send Error
            return null
        }

        // 初始化方法解析器和返回值解析器
        if (this.argumentResolvers != null) {
            if (this.argumentResolvers!!.isEmpty()) {
                this.argumentResolvers = ArrayList(getDefaultArgumentResolvers())
            }
            val composite = HandlerMethodArgumentResolverComposite()
            composite.addArgumentResolvers(this.argumentResolvers!!)
            exceptionHandlerMethod.argumentResolvers = composite
        }
        if (this.returnValueHandlers != null) {
            if (this.returnValueHandlers!!.isEmpty()) {
                this.returnValueHandlers = ArrayList(getDefaultReturnValueHandlers())
            }
            val composite = HandlerMethodReturnValueHandlerComposite()
            composite.addReturnValueHandlers(this.returnValueHandlers!!)
            exceptionHandlerMethod.returnValueHandlers = composite
        }

        val mavContainer = ModelAndViewContainer()
        val webRequest = ServerWebRequest(request, response)

        // 获取cause列表，因为下层的异常有可能被上层抓了，因此得拿到所有的cause列表传递下去去进行匹配
        val exceptions = ArrayList<Throwable>()
        if (logger.isDebugEnabled) {
            logger.info("使用@ExceptionHandler[$exceptionHandlerMethod]去处理异常")
        }
        var exToExpose: Throwable? = ex
        while (exToExpose != null) {
            exceptions += exToExpose
            val cause = exToExpose.cause
            exToExpose = if (cause !== exToExpose) cause else null
        }

        // 构建候选的参数列表，为了后续解析参数可以解析到，这些参数都可以可以支持去注入给@ExceptionHandler方法
        val arguments = arrayOfNulls<Any>(exceptions.size + 1)
        // 1. 添加异常列表，使用这种方式，可以更好地去使用ArrayList当中的arraycopy
        exceptions.toArray(arguments)
        // 2. 添加HandlerMethod到候选的参数列表
        arguments[arguments.size - 1] = exceptionHandlerMethod

        // 执行目标ExceptionHandler方法...
        @Suppress("UNCHECKED_CAST")
        exceptionHandlerMethod.invokeAndHandle(webRequest, mavContainer, *(arguments as Array<Any>))

        // return an empty ModelAndView to pass next HandlerExceptionResolvers and not to render view...
        return ModelAndView()
    }

    /**
     * 获取ExceptionHandler的HandlerMethod
     *
     * @param handlerMethod HandlerMethod
     * @param ex     要去进行处理的异常
     * @return 找到的处理当前异常的ExceptionHandler的方法(如果获取不到return null)
     */
    protected open fun getExceptionHandlerMethod(
        handlerMethod: InvocableHandlerMethod?,
        ex: Throwable
    ): InvocableHandlerMethod? {

        // 首先，寻找当前Controller当中是否有合适的ExceptionHandler？
        val handlerType: Class<*>?
        if (handlerMethod != null) {
            handlerType = handlerMethod.beanType ?: throw IllegalStateException("HandlerMethod的beanType不能为null")
            val bean = handlerMethod.bean ?: throw IllegalStateException("HandlerMethod的目标对象不能为null")
            var methodResolver = exceptionHandlerCache[handlerType]
            if (methodResolver == null) {
                methodResolver = ExceptionHandlerMethodResolver(handlerType)
                exceptionHandlerCache[handlerType] = methodResolver
            }
            val resolveMethod = methodResolver.resolveMethod(ex)
            if (resolveMethod != null) {
                return InvocableHandlerMethod.newInvocableHandlerMethod(bean, resolveMethod)
            }
        }

        // 接着，尝试从ControllerAdvice的ExceptionHandler缓存当中去进行寻找
        exceptionHandlerAdviceCache.forEach { (bean, resolver) ->
            val resolveMethod = resolver.resolveMethod(ex) ?: return@forEach
            return InvocableHandlerMethod.newInvocableHandlerMethod(bean.resolveBean(), resolveMethod)
        }

        // 如果还是找不到，那么return null
        return null
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
     *
     * @return 默认的HandlerMethodArgumentResolver列表
     */
    private fun getDefaultArgumentResolvers(): List<HandlerMethodArgumentResolver> {
        val resolvers = ArrayList<HandlerMethodArgumentResolver>()

        // 添加@RequestParam、@RequestHeader的参数解析器
        resolvers += RequestHeaderMethodArgumentResolver()
        resolvers += RequestParamMethodArgumentResolver()

        // 处理ServerRequest和ServerResponse的参数处理器
        resolvers += ServerRequestMethodArgumentResolver()
        resolvers += ServerResponseMethodArgumentResolver()

        // 添加Model方法处理器，处理Model类型的参数，将ModelAndViewContainer当中的Model传递下去
        resolvers += ModelMethodProcessor()
        // 添加Map方法处理器，处理Map类型的参数，将ModelAndViewContainer当中的Model传递下去
        resolvers += MapMethodProcessor()

        // 添加RequestResponseBody的方法处理器(处理@RequestBody注解)
        resolvers += RequestResponseBodyMethodProcessor(getHttpMessageConverters(), getContentNegotiationManager())

        // 添加一个ModelAttribute的方法处理器，处理器方法参数上的@ModelAttribute注解，以及非简单类型的JavaBean
        resolvers += ModelAttributeMethodProcessor()

        // 应用用户自定义的参数解析器
        if (getCustomArgumentResolvers() != null) {
            resolvers += getCustomArgumentResolvers()!!
        }

        return resolvers
    }

    /**
     * 如果不进行自定义，那么需要去获取默认的ReturnValueHandlers列表
     *
     * @return 默认的HandlerMethodReturnValueHandler列表
     */
    private fun getDefaultReturnValueHandlers(): List<HandlerMethodReturnValueHandler> {
        val handlers = ArrayList<HandlerMethodReturnValueHandler>()

        // RequestResponseBody的方法处理器
        handlers += RequestResponseBodyMethodProcessor(getHttpMessageConverters(), getContentNegotiationManager())

        // 解析ModelAndView的返回值的方法处理器
        handlers += ModelAndViewMethodReturnValueHandler()

        // 添加Model方法处理器，去处理Model类型的返回值，将Model数据转移到ModelAndViewContainer当中
        handlers += ModelMethodProcessor()

        // 添加Map方法处理器，去处理Map类型的返回值，将Map数据转移到ModelAndViewContainer当中
        handlers += MapMethodProcessor()

        // 解析ViewName的处理器
        handlers += ViewNameMethodReturnValueHandler()

        // 添加一个ModelAttribute的方法处理器，处理方法返回值是ModelAttribute的情况
        handlers += ModelAttributeMethodProcessor()

        // 应用用户自定义的返回值处理器
        if (getCustomReturnValueHandlers() != null) {
            handlers += getCustomReturnValueHandlers()!!
        }
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

    open fun getCustomReturnValueHandlers(): List<HandlerMethodReturnValueHandler>? = this.customReturnValueHandlers

    open fun setCustomReturnValueHandlers(returnValueHandlers: List<HandlerMethodReturnValueHandler>) {
        this.customReturnValueHandlers = returnValueHandlers
    }

    open fun setHandlerMethodArgumentResolvers(resolvers: List<HandlerMethodArgumentResolver>) {
        this.argumentResolvers = resolvers
    }

    open fun setCustomArgumentResolvers(argumentResolvers: List<HandlerMethodArgumentResolver>) {
        this.customArgumentResolvers = argumentResolvers
    }

    open fun getCustomArgumentResolvers(): List<HandlerMethodArgumentResolver>? = this.customArgumentResolvers

    open fun getHttpMessageConverters(): List<HttpMessageConverter<*>> {
        return this.messageConverters
            ?: throw IllegalStateException("请先初始化RequestMappingHandlerAdapter的MessageConverter列表")
    }

    open fun setHandlerMethodReturnValueHandlers(handlers: List<HandlerMethodReturnValueHandler>) {
        this.returnValueHandlers = handlers
    }
}