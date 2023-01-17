package com.wanna.framework.web.handler

import com.wanna.framework.context.aware.BeanNameAware
import com.wanna.framework.core.Ordered
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.HandlerExecutionChain
import com.wanna.framework.web.HandlerInterceptor
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.HttpRequestHandler
import com.wanna.framework.web.context.WebApplicationObjectSupport
import com.wanna.framework.web.cors.*
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 这是一个抽象的HandlerMapping的实现, 为各个子类的HandlerMapping的实现提供了模板方法;
 *
 * ## 1.对于处理HTTP请求的支持
 *
 * * 1.它实现了ApplicationContextAware, 能自动注入ApplicationContext对象;
 * * 2.它负责将handler和HandlerInterceptor列表去封装成为HandlerExecutionChain, 它提供了对HandlerInterceptor列表的扩展方法, 可以交给子类去进行扩展;
 * * 3.它也支持去自动从ApplicationContext当中去侦测MappedHandlerInterceptor类型的Bean, 加入到HandlerInterceptor列表当中;
 * * 4.对于handler的具体的落地获取方式, 它为子类提供了getHandlerInternal方法,
 * 子类可以通过getHandlerInternal这个抽象模板方法去实现要子类当中要自定义的获取处理本次HTTP请求
 * 的Handler对象的方式, 比如可以在这里返回一个HandlerMethod对象作为处理请求的最终Handler
 *
 * ## 2.对于处理CORS请求处理的支持
 *
 * * 1.它为所有的子类, 提供了CorsConfiguration的配置信息的扩展的模板方法, 也在HandlerMapping层面,
 * 支持去进行Cors请求的处理, 子类可以在这个类的基础上, 去进行扩展CorsConfig, 比如在类和方法上去寻找@CorsOrigin注解;
 * * 2.对于CORS的"PreFlight"请求, 采用PreFlightHandler去替换掉真正的Handler的方式处理(委托CorsProcessor);
 * * 3.对于正常的CORS的请求, 采用添加一个HandlerInterceptor的方式去进行处理(委托CorsProcessor);
 *
 * @see getHandler
 * @see getHandlerInternal
 */
abstract class AbstractHandlerMapping : HandlerMapping, Ordered, BeanNameAware, WebApplicationObjectSupport() {
    /**
     * order of this HandlerMapping
     */
    private var order: Int = Ordered.ORDER_LOWEST

    /**
     * beanName of this HandlerMapping
     */
    private var beanName: String? = null

    /**
     * 在找不到处理请求的Handler时的默认使用的Handler
     */
    @Nullable
    private var defaultHandler: Any? = null

    /**
     * 拦截器列表, 可以放入非HandlerInterceptor类型的类型的拦截器, 在经过类型转换之后, 将会合并到adaptedInterceptors当中
     */
    private var interceptors: ArrayList<Any> = ArrayList()

    /**
     * 经过了类型转换之后的拦截器列表, 也是最终使用的HandlerInterceptor列表
     */
    private val adaptedInterceptors: ArrayList<HandlerInterceptor> = ArrayList()

    /**
     * 全局的HandlerMapping层面的Cors的ConfigurationSource
     */
    @Nullable
    private var corsConfigurationSource: CorsConfigurationSource? = null

    /**
     * CorsProcessor, 提供处理Cors跨域请求的处理器
     */
    private var corsProcessor: CorsProcessor = DefaultCorsProcessor()

    @Nullable
    open fun getDefaultHandler(): Any? = this.defaultHandler

    open fun setDefaultHandler(@Nullable defaultHandler: Any?) {
        this.defaultHandler = defaultHandler
    }

    /**
     * 实现HandlerMapping接口的getHandler方法, 为指定的request去找到合适的处理请求的Handler
     *
     * @param request request
     * @return 根据给定的request, 去找到的处理本次请求的HandlerExecutionChain(如果没有找到合适的Handler, return null)
     */
    @Nullable
    override fun getHandler(request: HttpServerRequest): HandlerExecutionChain? {
        // 1.尝试去寻找Handler, 如果没有合适的Handler的话, 采用默认的Handler, 如果也没有的话, return null
        var handler = getHandlerInternal(request) ?: getDefaultHandler() ?: return null
        // 如果返回的Handler是String, 那么去进行getBean
        handler = if (handler is String) obtainApplicationContext().getBean(handler) else handler

        // 获取HandlerExecutionChain(HandlerInterceptors & Handler)
        var handlerExecutionChain = getHandlerExecutionChain(request, handler)


        // 1.如果当前HandlerMapping当中, 确实存在有CorsConfigurationSource, 则说明需要去匹配跨域
        // 2.如果当前请求是浏览器发送的一个预检请求(OPTIONS), 那么也需要去处理跨域请求(把处理请求的Handler换掉)
        if (hasCorsConfigurationSource(handler) || CorsUtils.isPreFlightRequest(request)) {
            // 获取到HandlerMapping当中的GlobalCorsConfiguration
            val globalConfig = getCorsConfigurationSource()?.getCorsConfiguration(request)

            // 获取到Handler/HandlerMethod当中的的CorsConfiguration
            var config = getCorsConfiguration(request, handler)

            // 如果必要的话, 联合两个CorsConfig去进行合并
            config = globalConfig?.combine(config) ?: config

            // 获取带有Cors功能的HandlerExecutionChain
            handlerExecutionChain = getCorsHandlerExecutionChain(request, handlerExecutionChain, config)
        }
        return handlerExecutionChain
    }

    /**
     * 获取真正的内部的处理请求的Handler, 对于具体的实现交给子类去进行实现
     *
     * @param request request
     * @return 根据Request去找到的合适的处理当前请求的Handler(如果没有找到的话, return null)
     */
    @Nullable
    protected abstract fun getHandlerInternal(request: HttpServerRequest): Any?

    /**
     * 将Handler和所有的HandlerInterceptor包装到HandlerExecutionChain当中并进行return
     *
     * @param request request
     * @param handler handler
     * @return HandlerExecutionChain with Handler
     */
    protected open fun getHandlerExecutionChain(request: HttpServerRequest, handler: Any): HandlerExecutionChain {
        val chain = if (handler is HandlerExecutionChain) handler else HandlerExecutionChain(handler)
        this.adaptedInterceptors.forEach(chain::addInterceptor)
        return chain
    }

    /**
     * 获取支持去处理Cors的HandlerExecutionChain, 需要添加处理Cors的处理器
     *
     * * 1. 如果是个预检请求, 那么我们直接将处理请求的Handler换掉, 我们使用PreFlightHandler去处理本次请求
     * * 2. 如果它不是一个预检请求, 那么只需要添加一个CorsInterceptor去处理请求就行
     *
     * @param request request
     * @param chain HandlerExecutionChain
     * @param config CorsConfiguration
     * @return 支持去处理Cors的HandlerExecutionChain
     */
    protected open fun getCorsHandlerExecutionChain(
        request: HttpServerRequest,
        chain: HandlerExecutionChain,
        config: CorsConfiguration?
    ): HandlerExecutionChain {
        var chainToUse: HandlerExecutionChain = chain

        // 如果是个预检请求, 那么我们直接将处理请求的Handler换掉, 我们使用PreFlightHandler去处理本次请求
        if (CorsUtils.isPreFlightRequest(request)) {
            chainToUse = HandlerExecutionChain(PreFlightHandler(config), chain.getInterceptors())

            // 如果它不是一个预检请求, 那么只需要添加一个CorsInterceptor去处理请求就行
        } else {
            chainToUse.addInterceptor(0, CorsInterceptor(config))
        }
        return chainToUse
    }

    /**
     * 当前HandlerMapping当中, 是否存在有CorsConfigurationSource?
     * 默认情况下：只要当前HandlerMapping当中存在着CorsConfigurationSource,
     * 或者Handler本身就是CorsConfigurationSource类型, return true,
     * 子类也可以根据需要去进行扩展该功能
     *
     * @param handler handler(支持HandlerExecutionChain)
     * @return 如果有CorsConfigurationSource, return true; 否则return false
     */
    protected open fun hasCorsConfigurationSource(handler: Any): Boolean {
        val handlerToMatch = if (handler is HandlerExecutionChain) handler.getHandler() else handler
        return this.corsConfigurationSource != null || handlerToMatch is CorsConfigurationSource
    }

    /**
     * 针对具体的HandlerMethod, 去获取方法级别上的CorsConfiguration,
     * 我们默认情况下, 只去检查Handler是否是一个CorsConfiguration,
     * 对于更多的具体扩展的逻辑, 可以交给子类自行去进行扩展和完善
     *
     * @param request request
     * @param handler handler
     * @return 如果有CorsConfiguration的话, 那么return CorsConfiguration; 否则return null
     */
    @Nullable
    protected open fun getCorsConfiguration(request: HttpServerRequest, handler: Any): CorsConfiguration? {
        var resolvedHandler: Any? = handler
        if (handler is HandlerExecutionChain) {
            resolvedHandler = handler.getHandler()
        }
        if (resolvedHandler is CorsConfigurationSource) {
            return resolvedHandler.getCorsConfiguration(request)
        }
        return null
    }

    override fun setBeanName(beanName: String) {
        this.beanName = beanName
    }

    /**
     * 获取当前HandlerMapping的beanName
     *
     * @return beanName
     */
    open fun getBeanName(): String? = this.beanName

    override fun getOrder() = this.order

    open fun setOrder(order: Int) {
        this.order = order
    }

    override fun initApplicationContext() {
        extendsInterceptors(this.interceptors)
        detectMappedInterceptors(this.adaptedInterceptors)
        initInterceptors()
    }

    /**
     * 交给子类去进行重写, 去扩展Interceptors, 往给定的这个列表当中添加元素即可添加
     *
     * @param interceptors HandlerMapping的拦截器列表(输出参数, 可以往这个列表当中去添加元素)
     */
    protected open fun extendsInterceptors(interceptors: MutableList<Any>) {

    }

    /**
     * 从ApplicationContext当中去探测所有的MappedInterceptors, 加入到HandlerInterceptor列表当中
     *
     * @param mappedInterceptors interceptors列表, 对于最终的HandlerInterceptor会被添加到这个列表当中来
     */
    protected open fun detectMappedInterceptors(mappedInterceptors: MutableList<HandlerInterceptor>) {
        mappedInterceptors.addAll(obtainApplicationContext().getBeansForType(MappedInterceptor::class.java).values)
    }

    /**
     * 设置当前HandlerMapping的拦截器列表(如果之前已经有拦截器的话, 替换掉之前的所有)
     *
     * @param interceptors 你想要设置的Interceptor列表(可以为非HandlerInterceptor类型, 为了去支持别的类型的拦截器)
     */
    open fun setInterceptors(vararg interceptors: Any) {
        this.interceptors = arrayListOf(*interceptors)
    }

    /**
     * 设置CorsConfiguration配置信息, 将其封装成为一个CorsConfigurationSource;
     * 这个方法和`setCorsConfigurationSource`方法互斥, 使用其中一个时, 将会替换掉之前的配置信息;
     *
     * @param corsConfigurations 要使用的CorsConfigurations(key-pathPattern, value-CorsConfiguration)
     * @see setCorsConfigurationSource
     */
    open fun setCorsConfigurations(corsConfigurations: Map<String, CorsConfiguration>) {
        // 如果根本就没有CorsConfiguration, 那么将CorsConfigurationSource设为null
        if (corsConfigurations.isEmpty()) {
            this.corsConfigurationSource = null
            return
        }
        // 如果有CorsConfiguration的话, 那么将CorsConfigurations包装到CorsConfigurationSource
        val configurationSource = UrlBasedCorsConfigurationSource()
        configurationSource.setCorsConfigurations(corsConfigurations)
        // set CorsConfigurationSource
        setCorsConfigurationSource(configurationSource)
    }

    /**
     * 设置CorsConfiguration配置信息, 直接使用CorsConfigurationSource的方式去进行设置
     *
     * @param source CorsConfigurationSource
     */
    open fun setCorsConfigurationSource(source: CorsConfigurationSource) {
        this.corsConfigurationSource = source
    }

    /**
     * 获取CorsConfigurationSource
     */
    @Nullable
    open fun getCorsConfigurationSource(): CorsConfigurationSource? = this.corsConfigurationSource

    /**
     * 初始化Interceptors列表, 用于将interceptors列表当中的拦截器列表转换为HandlerInterceptor
     */
    protected open fun initInterceptors() {
        if (this.interceptors.isEmpty()) {
            return
        }
        this.interceptors.forEach {
            if (it is HandlerInterceptor) {
                this.adaptedInterceptors += it
            }
        }
    }

    /**
     * 处理Cors的PreFlight预检请求的RequestHandler
     *
     * @param config CorsConfig
     */
    open inner class PreFlightHandler(@Nullable private val config: CorsConfiguration?) : HttpRequestHandler,
        CorsConfigurationSource {

        /**
         * 根据给定的[CorsConfiguration], 使用[CorsProcessor]去真正地处理请求
         *
         * @param request request
         * @param response response
         */
        override fun handleRequest(request: HttpServerRequest, response: HttpServerResponse) {
            corsProcessor.processRequest(request, response, config)
        }

        /**
         * 获取Cors的配置信息
         *
         * @param request request
         * @return Cors的配置信息
         */
        @Nullable
        override fun getCorsConfiguration(request: HttpServerRequest) = this.config
    }

    /**
     * 处理Cors的[HandlerInterceptor], 将其委托给CorsProcessor去进行处理, Interceptor是一层桥接
     *
     * @param config CorsConfig
     * @see CorsProcessor
     */
    open inner class CorsInterceptor(@Nullable private val config: CorsConfiguration?) : HandlerInterceptor,
        CorsConfigurationSource {

        @Nullable
        override fun getCorsConfiguration(request: HttpServerRequest) = this.config

        override fun preHandle(request: HttpServerRequest, response: HttpServerResponse, handler: Any): Boolean {
            return corsProcessor.processRequest(request, response, config)
        }

        override fun postHandle(request: HttpServerRequest, response: HttpServerResponse, handler: Any) {}

        override fun afterCompletion(
            request: HttpServerRequest,
            response: HttpServerResponse,
            handler: Any,
            ex: Throwable?
        ) {

        }
    }


}