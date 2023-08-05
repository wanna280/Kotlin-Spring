package com.wanna.framework.web.method.annotation

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.ControllerAdviceBean
import com.wanna.framework.web.method.HandlerMethod
import com.wanna.framework.web.method.support.*
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.common.logging.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个@ExceptionHandler的HandlerExceptionResolver, 采用@ExceptionHandler的方式去处理处理请求当中发生的异常;
 *
 * * 1.支持从@ControllerAdvice的Bean上去寻找@ExceptionHandler注解标注的方法
 * * 2.支持从Controller的Bean上去寻找@ExceptionHandler
 *
 * @see HandlerExceptionResolver
 */
open class ExceptionHandlerExceptionResolver : HandlerExceptionResolver, ApplicationContextAware, InitializingBean {
    companion object {

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ExceptionHandlerMethodResolver::class.java)
    }

    /**
     * 消息转换器列表
     */
    private var messageConverters: List<HttpMessageConverter<*>>? = null

    /**
     *  处理@ExceptionHandler的自定义的参数解析器(基于默认的去进行扩展)
     */
    @Nullable
    private var customArgumentResolvers: List<HandlerMethodArgumentResolver>? = null

    /**
     * 处理@ExceptionHandler方法的自定义的返回值处理器(基于默认的去进行扩展)
     */
    @Nullable
    private var customReturnValueHandlers: List<HandlerMethodReturnValueHandler>? = null

    /**
     * 处理@ExceptionHandler的参数解析器
     */
    @Nullable
    private var argumentResolvers: List<HandlerMethodArgumentResolver>? = null

    /**
     * 处理@ExceptionHandler的返回值的处理器
     */
    @Nullable
    private var returnValueHandlers: List<HandlerMethodReturnValueHandler>? = null

    /**
     *  内容协商管理器
     */
    private var contentNegotiationManager: ContentNegotiationManager = ContentNegotiationManager()

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    /**
     * ExceptionHandler的缓存, key-handlerType(Controller), value-ExceptionHandlerMethodResolver
     */
    private val exceptionHandlerCache = ConcurrentHashMap<Class<*>, ExceptionHandlerMethodResolver>(64)

    /**
     * ExceptionHandlerAdviceMap(key-ControllerAdviceBean, value-ExceptionHandlerMethodResolver)
     */
    private val exceptionHandlerAdviceCache =
        ConcurrentHashMap<ControllerAdviceBean, ExceptionHandlerMethodResolver>(64)

    /**
     * 解析给定的异常信息, 和Handler的处理方式类似, 也是经过参数解析器、返回值处理器等的处理, 最终返回一个ModelAndView对象
     *
     * @param request request
     * @param response response
     * @param handler handler
     * @param ex ex
     * @return 处理异常之后, 得到的ModelAndView数据
     */
    override fun resolveException(
        request: HttpServerRequest, response: HttpServerResponse, handler: Any?, ex: Throwable
    ): ModelAndView? {

        // 遍历所有的ControllerAdvice, 去寻找合适的ExceptionHandler去处理异常
        val exceptionHandlerMethod = getExceptionHandlerMethod(handler as HandlerMethod?, ex)
        if (exceptionHandlerMethod == null) {
            response.sendError(500) // send Error
            return null
        }
        var argumentResolvers = this.argumentResolvers
        var returnValueHandlers = this.returnValueHandlers
        // 初始化@ExceptionHandler方法解析器和返回值解析器
        if (argumentResolvers != null) {
            if (argumentResolvers.isEmpty()) {
                argumentResolvers = ArrayList(getDefaultArgumentResolvers())
                this.argumentResolvers = argumentResolvers
            }
            val composite = HandlerMethodArgumentResolverComposite().addArgumentResolvers(argumentResolvers)
            exceptionHandlerMethod.argumentResolvers = composite
        }
        if (returnValueHandlers != null) {
            if (returnValueHandlers.isEmpty()) {
                returnValueHandlers = ArrayList(getDefaultReturnValueHandlers())
                this.returnValueHandlers = returnValueHandlers
            }
            val composite = HandlerMethodReturnValueHandlerComposite().addReturnValueHandlers(returnValueHandlers)
            exceptionHandlerMethod.returnValueHandlers = composite
        }

        val mavContainer = ModelAndViewContainer()
        val webRequest = ServerWebRequest(request, response)
        // 获取cause列表, 因为下层的异常有可能被上层抓了, 因此得拿到所有的cause列表传递给@ExceptionHandler的方法去进行匹配
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

        // 构建候选的参数列表, 为了后续解析参数可以解析到, 这些参数都可以可以支持去注入给@ExceptionHandler方法
        val arguments = arrayOfNulls<Any>(exceptions.size + 1)
        // 1. 添加异常列表, 使用这种方式, 可以更好地去使用ArrayList当中的arraycopy
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
        handlerMethod: HandlerMethod?,
        ex: Throwable
    ): InvocableHandlerMethod? {

        // 首先, 寻找当前Controller当中是否有合适的ExceptionHandler?
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
                return InvocableHandlerMethod(bean, resolveMethod)
            }
        }

        // 接着, 尝试从ControllerAdvice的ExceptionHandler缓存当中去进行寻找
        exceptionHandlerAdviceCache.forEach { (bean, resolver) ->
            val resolveMethod = resolver.resolveMethod(ex) ?: return@forEach
            return InvocableHandlerMethod(bean.resolveBean(), resolveMethod)
        }

        // 如果还是找不到, 那么return null
        return null
    }

    override fun afterPropertiesSet() {
        // 初始化ExceptionHandler
        initExceptionHandlerAdviceCache()
    }

    /**
     * 从SpringBeanFactory当中找到所有的@ControllerAdvice的Bean, 并找出所有的@ExceptionHandler方法
     */
    private fun initExceptionHandlerAdviceCache() {
        this.applicationContext ?: return

        // 从ApplicationContext当中, 找到所有的标注了@ControllerAdvice的Bean
        val controllerAdviceBeans = ControllerAdviceBean.findAnnotatedBeans(this.applicationContext!!)

        // 根据所有的ControllerAdviceBean, 去注册到ExceptionHandlerAdviceCache当中
        controllerAdviceBeans.forEach {
            val handlerMethodResolver = ExceptionHandlerMethodResolver(it.getBeanType())
            if (handlerMethodResolver.hasExceptionMappings()) {
                exceptionHandlerAdviceCache[it] = handlerMethodResolver
            }
        }
        if (logger.isDebugEnabled) {
            logger.debug("寻找到@ControllerAdvice[${exceptionHandlerAdviceCache.size}]个存在有@ExceptionHandler")
        }
    }

    /**
     * 如果不进行自定义, 那么需要去获取默认的HandlerMethod的参数解析器列表
     *
     * @return 默认的HandlerMethodArgumentResolver列表
     */
    private fun getDefaultArgumentResolvers(): List<HandlerMethodArgumentResolver> {
        val resolvers = ArrayList<HandlerMethodArgumentResolver>()

        // 添加@RequestParam、@RequestHeader、@CookieValue的参数解析器
        resolvers += RequestHeaderMethodArgumentResolver()
        resolvers += RequestParamMethodArgumentResolver()
        resolvers += ServerCookieValueMethodArgumentResolver()

        // 添加处理路径变量的参数解析器
        resolvers += PathVariableHandlerMethodArgumentResolver()
        resolvers += PathVariableMapHandlerMethodArgumentResolver()

        // 处理ServerRequest和ServerResponse的参数处理器
        resolvers += ServerRequestMethodArgumentResolver()
        resolvers += ServerResponseMethodArgumentResolver()

        // 添加Model方法处理器, 处理Model类型的参数, 将ModelAndViewContainer当中的Model传递下去
        resolvers += ModelMethodProcessor()
        // 添加Map方法处理器, 处理Map类型的参数, 将ModelAndViewContainer当中的Model传递下去
        resolvers += MapMethodProcessor()

        // 添加RequestResponseBody的方法处理器(处理@RequestBody注解)
        resolvers += RequestResponseBodyMethodProcessor(getHttpMessageConverters(), getContentNegotiationManager())

        // 添加一个ModelAttribute的方法处理器, 处理器方法参数上的@ModelAttribute注解, 以及非简单类型的JavaBean
        resolvers += ModelAttributeMethodProcessor()

        // 应用用户自定义的参数解析器
        if (getCustomArgumentResolvers() != null) {
            resolvers += getCustomArgumentResolvers()!!
        }

        return resolvers
    }

    /**
     * 如果不进行自定义, 那么需要去获取默认的ReturnValueHandlers列表
     *
     * @return 默认的HandlerMethodReturnValueHandler列表
     */
    private fun getDefaultReturnValueHandlers(): List<HandlerMethodReturnValueHandler> {
        val handlers = ArrayList<HandlerMethodReturnValueHandler>()


        // 解析ModelAndView的返回值的方法处理器
        handlers += ModelAndViewMethodReturnValueHandler()

        // 添加Model方法处理器, 去处理Model类型的返回值, 将Model数据转移到ModelAndViewContainer当中
        handlers += ModelMethodProcessor()

        // 添加一个处理返回值类型是Callable的返回值类型处理器, 负责将Callable转换成为异步任务去进行执行
        handlers += CallableMethodReturnValueHandler()

        // 添加一个处理返回值类型为DeferredResult/CompletableFuture/ListenableFuture的处理器, 负责去执行异步任务
        handlers += DeferredResultMethodReturnValueHandler()

        // 添加一个ModelAttribute的方法处理器, 处理方法返回值是ModelAttribute的类型
        handlers += ModelAttributeMethodProcessor(false)

        // RequestResponseBody的方法处理器
        handlers += RequestResponseBodyMethodProcessor(getHttpMessageConverters(), getContentNegotiationManager())

        // 解析ViewName的处理器(处理返回值是字符串的情况)
        handlers += ViewNameMethodReturnValueHandler()

        // 添加Map方法处理器, 去处理Map类型的返回值, 将Map数据转移到ModelAndViewContainer当中
        handlers += MapMethodProcessor()

        // 应用用户自定义的返回值处理器
        if (getCustomReturnValueHandlers() != null) {
            handlers += getCustomReturnValueHandlers()!!
        }

        // 添加一个ModelAttribute的方法处理器, 处理方法返回值是ModelAttribute的类型/不是简单类型的返回值类型
        handlers += ModelAttributeMethodProcessor(true)
        return handlers
    }

    /**
     * 设置ApplicationContext
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 获取内容协商管理器
     *
     * @return 内容协商管理器
     */
    open fun getContentNegotiationManager(): ContentNegotiationManager = this.contentNegotiationManager

    /**
     * 自定义内容协商管理器(提供解析客户端想要接收什么类型的响应信息, 默认是解析Header当中的"Accept"字段)
     *
     * @param contentNegotiationManager 你想要使用的内容协商管理器
     */
    open fun setContentNegotiationManager(contentNegotiationManager: ContentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager
    }

    /**
     * 自定义MessageConverters
     *
     * @param converters 你想要使用的MessageConverters
     */
    open fun setHttpMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        this.messageConverters = converters
    }

    /**
     * 获取[HttpMessageConverter]列表, 提供消息的转换
     *
     * @return [HttpMessageConverter]消息转换器列表
     */
    open fun getHttpMessageConverters(): List<HttpMessageConverter<*>> {
        return this.messageConverters
            ?: throw IllegalStateException("请先初始化RequestMappingHandlerAdapter的MessageConverter列表")
    }

    /**
     * 设置返回值处理器(直接替换掉之前的全部)
     *
     * @param handlers 返回值处理器列表
     */
    open fun setHandlerMethodReturnValueHandlers(handlers: List<HandlerMethodReturnValueHandler>?) {
        this.returnValueHandlers = handlers
    }

    /**
     * 获取自定义的返回值处理器[HandlerMethodReturnValueHandler]列表
     *
     * @return 自定义的返回值处理器列表
     */
    open fun getCustomReturnValueHandlers(): List<HandlerMethodReturnValueHandler>? = this.customReturnValueHandlers

    /**
     * 设置自定义的返回值处理器[HandlerMethodReturnValueHandler]
     *
     * @param returnValueHandlers 你想要使用的自定义的返回值处理器
     */
    open fun setCustomReturnValueHandlers(returnValueHandlers: List<HandlerMethodReturnValueHandler>?) {
        this.customReturnValueHandlers = returnValueHandlers
    }

    /**
     * 设置参数解析器[HandlerMethodArgumentResolver] (直接替换掉之前的全部)
     *
     * @param resolvers HandlerMethodArgumentResolvers
     */
    open fun setHandlerMethodArgumentResolvers(resolvers: List<HandlerMethodArgumentResolver>?) {
        this.argumentResolvers = resolvers
    }

    /**
     * 设置自定义的参数解析器[HandlerMethodArgumentResolver]列表
     *
     * @param argumentResolvers 自定义的HandlerMethodArgumentResolvers
     */
    open fun setCustomArgumentResolvers(argumentResolvers: List<HandlerMethodArgumentResolver>?) {
        this.customArgumentResolvers = argumentResolvers
    }

    /**
     * 获取自定义的参数解析器[HandlerMethodArgumentResolver]列表
     *
     * @return 自定义的参数解析器列表
     */
    open fun getCustomArgumentResolvers(): List<HandlerMethodArgumentResolver>? = this.customArgumentResolvers
}