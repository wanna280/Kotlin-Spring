package com.wanna.framework.web.method.annotation

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.AbstractHandlerMethodAdapter
import com.wanna.framework.web.method.HandlerMethod
import com.wanna.framework.web.method.support.*
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

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
open class RequestMappingHandlerAdapter : AbstractHandlerMethodAdapter(), BeanFactoryAware, InitializingBean {

    // beanFactory，通过BeanFactoryAware去进行自动回调
    private var beanFactory: BeanFactory? = null

    // 参数名发现器，用来去对方法的参数名去进行获取
    private var parameterNameDiscoverer = DefaultParameterNameDiscoverer()

    // 交给外部去进行自定义的参数解析器(基于默认的去进行扩展)
    private var customArgumentResolvers: List<HandlerMethodArgumentResolver>? = null

    // 交给外部去机械能自定义的返回值处理器(基于默认的去进行扩展)
    private var customReturnValueHandlers: List<HandlerMethodReturnValueHandler>? = null

    // 参数解析器列表(内部组合)
    private var argumentResolvers: HandlerMethodArgumentResolverComposite? = null

    // 返回值解析器列表(内部组合)
    private var returnValueHandlers: HandlerMethodReturnValueHandlerComposite? = null

    // 支持对HTTP请求的RequestBody和ResponseBody去进行消息转换的HttpMessageConverter列表
    private var messageConverters: MutableList<HttpMessageConverter<*>>? = null

    // 内容协商管理器
    private var contentNegotiationManager: ContentNegotiationManager = ContentNegotiationManager()

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    /**
     * 在Bean初始化时，需要初始化内部的参数解析器和返回值解析器列表，方便后续使用HandlerAdapter去处理目标请求
     */
    override fun afterPropertiesSet() {
        if (this.returnValueHandlers == null) {
            val handlers = HandlerMethodReturnValueHandlerComposite()
            handlers.addReturnValueHandlers(getDefaultReturnValueHandlers())
            this.returnValueHandlers = handlers
        }
        if (this.argumentResolvers == null) {
            val resolvers = HandlerMethodArgumentResolverComposite()
            resolvers.addArgumentResolvers(getDefaultArgumentResolvers())
            this.argumentResolvers = resolvers
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

        // 构建InvocableHandlerMethod，并去完成参数名发现器、参数解析器以及返回值处理器的初始化
        val invocableHandlerMethod = InvocableHandlerMethod.newInvocableHandlerMethod(handler)
        invocableHandlerMethod.parameterNameDiscoverer = parameterNameDiscoverer
        if (this.argumentResolvers != null) {
            invocableHandlerMethod.argumentResolvers = this.argumentResolvers
        }
        if (this.returnValueHandlers != null) {
            invocableHandlerMethod.returnValueHandlers = this.returnValueHandlers
        }

        // 执行HandlerMethod
        invocableHandlerMethod.invokeAndHandle(serverWebRequest)
        return null
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

        // 应用自定义的参数解析器
        if (getCustomArgumentResolvers() != null) {
            resolvers += getCustomArgumentResolvers()!!
        }
        return resolvers
    }

    /**
     * 如果不进行自定义，那么需要去获取默认的ReturnValueHandlers列表
     */
    private fun getDefaultReturnValueHandlers(): List<HandlerMethodReturnValueHandler> {
        val handlers = ArrayList<HandlerMethodReturnValueHandler>()
        handlers += RequestResponseBodyMethodProcessor(getHttpMessageConverters(), getContentNegotiationManager())

        // 应用默认的返回值处理器
        if (getCustomReturnValueHandlers() != null) {
            handlers += getCustomReturnValueHandlers()!!
        }
        return handlers
    }

    open fun setCustomArgumentResolvers(argumentResolvers: List<HandlerMethodArgumentResolver>) {
        this.customArgumentResolvers = argumentResolvers
    }

    open fun getCustomArgumentResolvers(): List<HandlerMethodArgumentResolver>? {
        return this.customArgumentResolvers
    }

    open fun setCustomReturnValueHandlers(returnValueHandlers: List<HandlerMethodReturnValueHandler>) {
        this.customReturnValueHandlers = returnValueHandlers
    }

    open fun getCustomReturnValueHandlers(): List<HandlerMethodReturnValueHandler>? {
        return this.customReturnValueHandlers
    }

    open fun setHttpMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        this.messageConverters = converters
    }

    open fun setHandlerMethodArgumentResolvers(resolvers: HandlerMethodArgumentResolverComposite) {
        this.argumentResolvers = resolvers
    }

    open fun getHttpMessageConverters(): List<HttpMessageConverter<*>> {
        if (this.messageConverters == null) {
            throw IllegalStateException("请先初始化RequestMappingHandlerAdapter的MessageConverter列表")
        }
        return this.messageConverters!!
    }

    open fun setHandlerMethodReturnValueHandlers(handlers: HandlerMethodReturnValueHandlerComposite) {
        this.returnValueHandlers = handlers
    }

    open fun getContentNegotiationManager(): ContentNegotiationManager {
        return this.contentNegotiationManager
    }

    open fun setContentNegotiationManager(contentNegotiationManager: ContentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager
    }
}