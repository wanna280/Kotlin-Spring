package com.wanna.framework.web.method.annotation

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.task.SimpleAsyncTaskExecutor
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.bind.annotation.InitBinder
import com.wanna.framework.web.bind.annotation.ModelAttribute
import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.bind.support.DefaultWebDataBinderFactory
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.context.request.async.WebAsyncUtils
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.AbstractHandlerMethodAdapter
import com.wanna.framework.web.method.ControllerAdviceBean
import com.wanna.framework.web.method.HandlerMethod
import com.wanna.framework.web.method.support.*
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

/**
 *
 * ## 关于RequestMappingHandlerAdapter的描述
 * 这是一个处理@RequestMapping注解的HandlerAdapter，它支持去处理之前获取到的HandlerExecutionChain当中的Handler为HandlerMethod的情况；
 * 它内部会聚合-->参数名发现器、参数解析器以及返回值解析器，在invokeHandlerMethod当中，会将HandlerMethod包装成为InvocableHandlerMethod，
 * 并将HandlerAdapter当中的参数名发现器、参数解析器以及返回值解析器全部转移到InvocableHandlerMethod当中，后续通过InvocableHandlerMethod
 * 去执行目标方法时，就可以应用所有的参数解析器去完成HandlerMethod的参数处理，应用所有的返回值处理器去完成HandlerMethod的返回值的处理；
 *
 * ## 对于HttpMessageConverter
 * 在初始化参数解析器/返回值处理器时，如果该参数解析器/返回值处理器需要MessageConverter，那么在初始化时，会自动将MessageConverter转移到对应的
 * 参数解析器/返回值处理器当中，因此如果想要自定义MessageConverter，需要在**初始化Bean(afterPropertiesSet方法)之前**，也就是在new对象的时候就得
 * 将要使用的MessageConverter(包括参数名解析器/返回值处理器也是一样)设置到RequestMappingHandlerAdapter当中，如果设置晚了，那么不生效！
 *
 * @see invokeHandlerMethod
 * @see com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
 * @see com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
 */
open class RequestMappingHandlerAdapter : AbstractHandlerMethodAdapter(), BeanFactoryAware, InitializingBean,
    ApplicationContextAware {

    companion object {
        // @InitBinder的方法过滤器
        private val INIT_BINDER_METHODS =
            Predicate<Method> { AnnotatedElementUtils.hasAnnotation(it, InitBinder::class.java) }

        // @ModelAttribute的方法过滤器(需要匹配没有@RequestMapping注解的方法)
        private val MODEL_ATTRIBUTE_METHODS = Predicate<Method> {
            AnnotatedElementUtils.hasAnnotation(
                it, ModelAttribute::class.java
            ) && !AnnotatedElementUtils.hasAnnotation(it, RequestMapping::class.java)
        }

        private val jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper")
    }

    // beanFactory，通过BeanFactoryAware去进行自动回调获取到
    private var beanFactory: ConfigurableBeanFactory? = null

    // ApplicationContext，通过ApplicationContextAware去进行自动回调获取到
    private var applicationContext: ApplicationContext? = null

    // 参数名发现器，用来去对方法的参数名去进行获取
    private var parameterNameDiscoverer = DefaultParameterNameDiscoverer()

    // 交给外部去进行自定义的参数解析器(基于默认的去进行扩展)
    private var customArgumentResolvers: List<HandlerMethodArgumentResolver>? = null

    // 交给外部去进行自定义的返回值处理器(基于默认的去进行扩展)
    private var customReturnValueHandlers: List<HandlerMethodReturnValueHandler>? = null

    // 参数解析器列表(内部组合)
    private var argumentResolvers: HandlerMethodArgumentResolverComposite? = null

    // @InitBinder的参数解析器列表，在调用@InitBinder方法时也需要解析方法参数...
    private var initBinderArgumentResolvers: HandlerMethodArgumentResolverComposite? = null

    // 处理目标RequestMapping方法(HandlerMethod)的返回值解析器列表(内部组合)
    private var returnValueHandlers: HandlerMethodReturnValueHandlerComposite? = null

    // 支持对HTTP请求的RequestBody和ResponseBody去进行消息转换的HttpMessageConverter列表
    private var messageConverters: MutableList<HttpMessageConverter<*>>? = null

    // 内容协商管理器，负责解析出来客户端想要接收的数据的媒体类型，服务端好尽可能去进行匹配...
    private var contentNegotiationManager: ContentNegotiationManager = ContentNegotiationManager()

    // 全局的@InitBinder方法的ControllerAdvice缓存，可以根据ControllerAdviceBean，去获取到@InitBinder的方法列表
    private val initBinderAdviceCache = LinkedHashMap<ControllerAdviceBean, Set<Method>>(64)

    // 针对于某个Controller(Handler)内部的@InitBinder方法的缓存，可以根据handlerType去获取到对应的@InitBinder缓存
    private val initBinderCache = ConcurrentHashMap<Class<*>, Set<Method>>(64)

    // 全局的@ModelAttribute方法的ControllerAdvice缓存，可以根据ControllerAdviceBean，去获取到@ModelAttribute的方法列表
    private val modelAttributeAdviceBean = LinkedHashMap<ControllerAdviceBean, Set<Method>>(64)

    // 针对某个Controller(Handler)内部的@ModelAttribute方法的缓存，可以根据handlerType去获取到对应的@ModelAttribute缓存
    private val modelAttributeCache = ConcurrentHashMap<Class<*>, Set<Method>>(64)

    // 处理异步任务的TaskExecutor
    private var asyncTaskExecutor = SimpleAsyncTaskExecutor("MvcAsync")

    /**
     * * 1.初始化ControllerAdvice缓存，构建@InitBinder和@ModelAttribute缓存
     * * 2.初始化Handler方法的返回处理器和参数解析器
     * * 3.初始化@InitBinder的参数解析器(不需要返回值解析器)
     */
    override fun afterPropertiesSet() {
        // 1.初始化ControllerAdvice缓存，处理@InitBinder方法和@ModelAttribute方法
        initControllerAdviceCache()

        // 2.初始化HandlerMethod的返回值处理器
        if (this.returnValueHandlers == null) {
            this.returnValueHandlers =
                HandlerMethodReturnValueHandlerComposite().addReturnValueHandlers(getDefaultReturnValueHandlers())
        }

        // 3.初始化@InitBinder方法的参数解析器
        if (this.initBinderArgumentResolvers == null) {
            this.initBinderArgumentResolvers =
                HandlerMethodArgumentResolverComposite().addArgumentResolvers(getDefaultInitBinderArgumentResolvers())
        }

        // 4.初始化HandlerMethod的参数解析器
        if (this.argumentResolvers == null) {
            this.argumentResolvers =
                HandlerMethodArgumentResolverComposite().addArgumentResolvers(getDefaultArgumentResolvers())
        }
    }

    override fun supportInternal(handler: HandlerMethod): Boolean {
        return true
    }

    override fun handleInternal(
        request: HttpServerRequest, response: HttpServerResponse, handler: HandlerMethod
    ): ModelAndView? {
        return invokeHandlerMethod(request, response, handler)
    }

    /**
     * 根据HandlerMethod去构建InvocableHandlerMethod(可以执行的HandlerMethod)，并调用它的invokeAndHandle方法，去处理本次请求
     *
     * @param request request
     * @param response response
     * @param handler HandlerMethod
     * @return mav
     */
    protected open fun invokeHandlerMethod(
        request: HttpServerRequest, response: HttpServerResponse, handler: HandlerMethod
    ): ModelAndView? {
        // 将request和response封装到NativeWebRequest当中
        val serverWebRequest = ServerWebRequest(request, response)

        // 创建WebDataBinderFactory，并往其中添加自定义的@InitBinder的方法，在调用createBinder方法时，会自动apply所有的InitBinder方法
        // @InitBinder方法的参数解析器，需要使用特殊定制的参数解析器(大致和普通的@RequestMapping方法参数解析器一致)
        val binderFactory = getDataBinderFactory(handler)

        // 获取ModelFactory，往其中加入自定义的@ModelAttribute方法，在ModelAndViewContainer初始化完成之后，自动将其中的数据合并到ModelAndViewContainer当中
        // @ModelAttribute方法的参数解析器，使用最普通的@RequestMapping的参数解析器即可
        val modelFactory = getModelFactory(handler, binderFactory)

        // 构建InvocableHandlerMethod，并去完成参数名发现器、DataBinderFactory、参数解析器以及返回值处理器的初始化
        var invocableHandlerMethod = InvocableHandlerMethod(handler)
        invocableHandlerMethod.binderFactory = binderFactory
        invocableHandlerMethod.parameterNameDiscoverer = parameterNameDiscoverer
        if (this.argumentResolvers != null) {
            invocableHandlerMethod.argumentResolvers = this.argumentResolvers
        }
        if (this.returnValueHandlers != null) {
            invocableHandlerMethod.returnValueHandlers = this.returnValueHandlers
        }

        // 创建一个ModelAndViewContainer，提前去存储ModelAndView的数据，因为之前ModelAndView并未创建，就需要一个渠道去存放之前的ModelAndView数据
        val mavContainer = ModelAndViewContainer()
        // 使用ModelFactory去初始化ModelAndViewContainer当中的Model数据，将@ModelAttribute方法当中的全部Model数据，全部apply到mavContainer当中
        modelFactory.initModel(serverWebRequest, mavContainer, invocableHandlerMethod)

        // 构建出来一个异步的WebAsyncRequest
        val asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response)

        // 获取到request当中的WebAsyncManager
        val asyncManager = WebAsyncUtils.getAsyncManager(request)
        // 将构建好的AsyncWebRequest设置到WebAsyncManager当中，方便后续去进行异步结果的派发
        asyncManager.setAsyncWebRequest(asyncWebRequest)
        // 设置处理异步任务的TaskExecutor，用于处理返回值为Callable时需要用到
        asyncManager.setAsyncTaskExecutor(this.asyncTaskExecutor)

        // 如果有异步结果了，那么需要去处理异步任务的结果，构建一个新的HandlerMethod，去替换掉原来的HandlerMethod
        // 不管怎么异步，最终的任务都得在这里去进行最终的处理工作...不会在别的地方去进行返回值的处理工作
        if (asyncManager.hasConcurrentResult()) {

            // 获取并发任务的执行结果
            val concurrentResult = asyncManager.getConcurrentResult()

            // 替换掉原来的HandlerMethod，只需要去处理返回值即可，对于参数等情况，完全不必去进行处理(Callable没有方法参数)
            invocableHandlerMethod = invocableHandlerMethod.wrapConcurrentResult(concurrentResult)
        }

        // 执行目标RequestMapping方法(HandlerMethod)，并获取到ModelAndView
        invocableHandlerMethod.invokeAndHandle(serverWebRequest, mavContainer)

        // 如果该异步任务已经并发启动了，那么return null，不需要getModelAndView
        if (asyncManager.isConcurrentHandlingStarted()) {
            return null
        }

        // 从ModelAndViewContainer当中获取到ModelAndView
        return getModelAndView(mavContainer, modelFactory, serverWebRequest)
    }

    /**
     * 从ModelAndViewContainer当中去获取ModelAndView
     *
     * * 1.如果请求已经被处理过了，那么return null(比如@ResponseBody)
     * * 2.如果请求还没被处理过，就需要创建ModelAndView，在后续当中去渲染视图
     */
    private fun getModelAndView(
        mavContainer: ModelAndViewContainer, modelFactory: ModelFactory, webRequest: ServerWebRequest
    ): ModelAndView? {
        if (mavContainer.requestHandled) {
            return null
        }
        val modelAndView = ModelAndView()
        modelAndView.view = mavContainer.view
        modelAndView.modelMap = mavContainer.defaultModel
        return modelAndView
    }

    /**
     * 创建WebBinderFactory，并应用所有的InitBinder方法
     *
     * * 1.应用全局的ControllerAdvice的@InitBinder方法(缓存中获取)
     * * 2.应用Controller内部的@InitBinder方法(如果必要的去构建，如果已经构建过了的话，从缓存当中获取)
     *
     * @param handler 要去执行的目标handlerMethod
     * @return 创建好的组合了@InitBinder的方法的WebDataBinderFactory
     */
    private fun getDataBinderFactory(handler: HandlerMethod): WebDataBinderFactory {
        val beanType = handler.beanType ?: throw IllegalStateException("HandlerMethod的beanType不能为null")
        val bean = handler.bean ?: throw IllegalStateException("HandlerMethod的bean不能为null")

        // 从Controller内部去解析@InitBinder，这些@InitBinder是应用给指定的具体的Controller(Handler)的...也就是局部的@InitBinder
        var cachedLocalMethods = initBinderCache[beanType]
        if (cachedLocalMethods == null) {
            val localBinderMethods = LinkedHashSet<Method>()
            ReflectionUtils.doWithMethods(beanType) {
                if (INIT_BINDER_METHODS.test(it)) {
                    localBinderMethods += it
                }
            }
            initBinderCache[beanType] = localBinderMethods  // put Cache
            cachedLocalMethods = localBinderMethods
        }

        // 1.根据全局的@InitBinder方法，去构建HandlerMethod列表
        val initMethods = ArrayList<InvocableHandlerMethod>()
        initBinderAdviceCache.forEach { (bean, methods) ->
            methods.forEach { initMethods += createInitBinderMethod(bean.resolveBean(), it) }
        }

        // 2.Controller内的局部@InitBinder方法列表，去构建HandlerMethod列表
        cachedLocalMethods.forEach {
            initMethods += createInitBinderMethod(bean, it)
        }
        // 创建DataBinderFactory
        return createDataBinderFactory(initMethods)
    }

    /**
     * 创建ModelFactory，并应用所有的ModelAttribute方法
     * * 1.应用全局的ControllerAdvice的@ModelAttribute方法(缓存中获取)
     * * 2.应用当前Controller内部的全部@ModelAttribute方法(如果必要的去构建，如果已经构建过了的话，从缓存当中获取)
     *
     * @param handlerMethod 要去执行的目标handlerMethod
     * @param binderFactory binderFactory
     * @return 包装有@ModelAttribute方法的ModelFactory
     */
    private fun getModelFactory(handlerMethod: HandlerMethod, binderFactory: WebDataBinderFactory): ModelFactory {
        val beanType = handlerMethod.beanType ?: throw IllegalStateException("HandlerMethod的beanType不能为null")
        val bean = handlerMethod.bean ?: throw IllegalStateException("HandlerMethod的Bean不能为null")
        var cachedLocalMethods = modelAttributeCache[beanType]
        if (cachedLocalMethods == null) {
            val localModelAttributeMethods = LinkedHashSet<Method>()
            ReflectionUtils.doWithMethods(beanType) {
                if (MODEL_ATTRIBUTE_METHODS.test(it)) {
                    localModelAttributeMethods += it
                }
            }
            modelAttributeCache[beanType] = localModelAttributeMethods  // put Cache
            cachedLocalMethods = localModelAttributeMethods
        }

        // 1. 根据全局的@ModelAttribute方法，去构建HandlerMethod
        val modelAttributeMethods = ArrayList<InvocableHandlerMethod>()
        modelAttributeAdviceBean.forEach { (bean, methods) ->
            methods.forEach {
                modelAttributeMethods += createModelAttributeMethod(
                    binderFactory, bean.resolveBean(), it
                )
            }
        }
        // 2. 根据Controller局部的@ModelAttribute方法，去构建HandlerMethod列表
        cachedLocalMethods.forEach {
            modelAttributeMethods += createModelAttributeMethod(binderFactory, bean, it)
        }

        // 构建ModelFactory
        return ModelFactory(modelAttributeMethods, binderFactory)
    }

    /**
     * 初始化ControllerAdvice缓存，扫描全部的ControllerAdvice中的@InitBinder和@ModelAndAttribute等方法；
     *
     * 这些ControllerAdvice，将会应用给所有的RequestMapping的HandlerMethod
     */
    private fun initControllerAdviceCache() {
        val applicationContext = getApplicationContext() ?: return
        val controllerAdviceBeans = ControllerAdviceBean.findAnnotatedBeans(applicationContext)
        // 处理所有的@ControllerAdvice，去完成注解扫描
        controllerAdviceBeans.forEach {
            val initBinderMethods = LinkedHashSet<Method>()
            val modelAttributeMethods = LinkedHashSet<Method>()
            ReflectionUtils.doWithMethods(it.getBeanType()) { method ->
                if (INIT_BINDER_METHODS.test(method)) {
                    initBinderMethods += method
                }
                if (MODEL_ATTRIBUTE_METHODS.test(method)) {
                    modelAttributeMethods += method
                }
            }

            // put Cache
            initBinderAdviceCache[it] = initBinderMethods
            modelAttributeAdviceBean[it] = modelAttributeMethods
        }
    }

    /**
     * 创建一个InitBinder的HandlerMethod
     *
     * @param bean bean(@InitBinder方法的Bean)
     * @param method method(@InitBinder方法)
     * @return 针对InitBinder方法去创建好的HandlerMethod
     */
    private fun createInitBinderMethod(bean: Any, method: Method): InvocableHandlerMethod {
        val handlerMethod = InvocableHandlerMethod(bean, method)
        // 初始化InitBinder的参数解析器
        if (this.initBinderArgumentResolvers != null) {
            handlerMethod.argumentResolvers = initBinderArgumentResolvers
        }
        handlerMethod.binderFactory = DefaultWebDataBinderFactory()
        handlerMethod.parameterNameDiscoverer = this.parameterNameDiscoverer
        return handlerMethod
    }

    /**
     * 创建ModelAttribute方法，并初始化参数名发现器、参数解析器、BinderFactory
     *
     * @param binderFactory BinderFactory
     * @param bean bean(@ModelAttribute方法所在的Bean)
     * @param method method(@ModelAttribute方法)
     */
    private fun createModelAttributeMethod(
        binderFactory: WebDataBinderFactory, bean: Any, method: Method
    ): InvocableHandlerMethod {
        val invocableHandlerMethod = InvocableHandlerMethod(bean, method)
        invocableHandlerMethod.parameterNameDiscoverer = this.parameterNameDiscoverer
        if (this.argumentResolvers != null) {
            invocableHandlerMethod.argumentResolvers = this.argumentResolvers
        }
        invocableHandlerMethod.binderFactory = binderFactory
        return invocableHandlerMethod
    }

    /**
     * 创建DataBinderFactory，去提供数据的绑定的支持，并将HandlerMethod列表完成初始化
     *
     * @param binderMethods @InitBinder的方法列表
     * @return 创建好的WebDataBinderFactory(InitBinderDataBinderFactory)
     */
    protected open fun createDataBinderFactory(binderMethods: List<InvocableHandlerMethod>): WebDataBinderFactory {
        return InitBinderDataBinderFactory(binderMethods)
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

        // 添加处理路径变量的参数解析器
        resolvers += PathVariableHandlerMethodArgumentResolver()
        resolvers += PathVariableMapHandlerMethodArgumentResolver()

        // 添加Model方法处理器，处理Model类型的参数，将ModelAndViewContainer当中的Model传递下去
        resolvers += ModelMethodProcessor()
        // 添加Map方法处理器，处理Map类型的参数，将ModelAndViewContainer当中的Model传递下去
        resolvers += MapMethodProcessor()

        // 添加一个处理Errors类型的方法参数的解析器，处理Errors/BindingResult这样的类型的参数
        resolvers += ErrorsMethodArgumentResolver()

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
     * 如果不进行自定义，那么需要去获取默认的HandlerMethod的参数解析器列表去完成@InitBinder方法的执行；
     * 在@InitBinder方法当中因为不存在ModelAndViewContainer，因此它无法处理Model相关的数据，因此没有Model相关的参数解析器
     *
     * @return 默认的HandlerMethodArgumentResolver列表
     */
    private fun getDefaultInitBinderArgumentResolvers(): List<HandlerMethodArgumentResolver> {
        val resolvers = ArrayList<HandlerMethodArgumentResolver>()

        // 添加@RequestParam、@RequestHeader的参数解析器
        resolvers += RequestHeaderMethodArgumentResolver()
        resolvers += RequestParamMethodArgumentResolver()

        // 处理ServerRequest和ServerResponse的参数处理器
        resolvers += ServerRequestMethodArgumentResolver()
        resolvers += ServerResponseMethodArgumentResolver()

        // 添加处理路径变量的参数解析器
        resolvers += PathVariableHandlerMethodArgumentResolver()
        resolvers += PathVariableMapHandlerMethodArgumentResolver()

        // 添加RequestResponseBody的方法处理器(处理@RequestBody注解)
        resolvers += RequestResponseBodyMethodProcessor(getHttpMessageConverters(), getContentNegotiationManager())

        // 应用用户自定义的参数解析器
        if (getCustomArgumentResolvers() != null) {
            resolvers += getCustomArgumentResolvers()!!
        }
        return resolvers
    }

    /**
     * 如果不进行自定义，那么需要去获取默认的ReturnValueHandlers列表
     *
     * Note: 这里的多个返回值处理器之间可能会发生冲突，需要安排合理的顺序...顺序是很关键的
     *
     * @return 默认的HandlerMethodReturnValueHandler列表
     */
    private fun getDefaultReturnValueHandlers(): List<HandlerMethodReturnValueHandler> {
        val handlers = ArrayList<HandlerMethodReturnValueHandler>()

        // 解析ModelAndView的返回值的方法处理器
        handlers += ModelAndViewMethodReturnValueHandler()

        // 添加Model方法处理器，去处理Model类型的返回值，将Model数据转移到ModelAndViewContainer当中
        handlers += ModelMethodProcessor()

        // 添加一个处理返回值类型是Callable的返回值类型处理器，负责将Callable转换成为异步任务去进行执行
        handlers += CallableMethodReturnValueHandler()

        // 添加一个处理返回值类型为DeferredResult/CompletableFuture/ListenableFuture的处理器，负责去执行异步任务
        handlers += DeferredResultMethodReturnValueHandler()

        // 添加一个ModelAttribute的方法处理器，处理方法返回值是ModelAttribute的类型(这里必须将参数设置为false，这里只去处理ModelAttribute)
        handlers += ModelAttributeMethodProcessor(false)

        // RequestResponseBody的方法处理器
        handlers += RequestResponseBodyMethodProcessor(getHttpMessageConverters(), getContentNegotiationManager())

        // 解析ViewName的处理器(处理返回值是字符串的情况)
        handlers += ViewNameMethodReturnValueHandler()

        // 添加Map方法处理器，去处理Map类型的返回值，将Map数据转移到ModelAndViewContainer当中
        handlers += MapMethodProcessor()

        // 应用用户自定义的返回值处理器
        if (getCustomReturnValueHandlers() != null) {
            handlers += getCustomReturnValueHandlers()!!
        }

        // 添加一个ModelAttribute的方法处理器，处理方法返回值是ModelAttribute的类型/不是简单类型的返回值类型
        handlers += ModelAttributeMethodProcessor(true)
        return handlers
    }

    open fun setCustomArgumentResolvers(argumentResolvers: List<HandlerMethodArgumentResolver>) {
        this.customArgumentResolvers = argumentResolvers
    }

    open fun getCustomArgumentResolvers(): List<HandlerMethodArgumentResolver>? = this.customArgumentResolvers

    open fun setCustomReturnValueHandlers(returnValueHandlers: List<HandlerMethodReturnValueHandler>) {
        this.customReturnValueHandlers = returnValueHandlers
    }

    open fun getCustomReturnValueHandlers(): List<HandlerMethodReturnValueHandler>? = this.customReturnValueHandlers

    open fun setHttpMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        this.messageConverters = converters
    }

    open fun setHandlerMethodArgumentResolvers(resolvers: HandlerMethodArgumentResolverComposite) {
        this.argumentResolvers = resolvers
    }

    open fun getHttpMessageConverters(): List<HttpMessageConverter<*>> =
        this.messageConverters ?: throw IllegalStateException("请先初始化RequestMappingHandlerAdapter的MessageConverter列表")

    open fun setHandlerMethodReturnValueHandlers(handlers: HandlerMethodReturnValueHandlerComposite) {
        this.returnValueHandlers = handlers
    }

    open fun getContentNegotiationManager(): ContentNegotiationManager = this.contentNegotiationManager

    open fun setContentNegotiationManager(contentNegotiationManager: ContentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        if (beanFactory is ConfigurableBeanFactory) {
            this.beanFactory = beanFactory
        }
    }

    open fun getBeanFactory(): ConfigurableBeanFactory? = this.beanFactory

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    open fun getApplicationContext(): ApplicationContext? = this.applicationContext
}