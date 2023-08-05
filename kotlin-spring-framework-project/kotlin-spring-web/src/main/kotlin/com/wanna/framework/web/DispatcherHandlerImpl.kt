package com.wanna.framework.web

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.ContextRefreshedEvent
import com.wanna.framework.context.event.SmartApplicationListener
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.io.support.PropertiesLoaderUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import com.wanna.framework.web.context.request.async.WebAsyncUtils
import com.wanna.framework.web.handler.HandlerAdapter
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.handler.ViewResolver
import com.wanna.framework.web.http.HttpStatus
import com.wanna.framework.web.method.RequestToViewNameTranslator
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.framework.web.ui.ModelMap
import com.wanna.framework.web.ui.View
import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.beans.BeansException
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException

/**
 * DispatcherHandler的具体实现
 */
open class DispatcherHandlerImpl : DispatcherHandler {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(DispatcherHandlerImpl::class.java)

        /**
         * 默认的策略路径, 如果从Spring BeanFactory当中没有探测到对应的组件, 那么可以从这个配置文件当中去进行读取默认配置
         */
        private const val DEFAULT_STRATEGIES_PATH = "DispatcherHandler.properties"

        /**
         *  默认的HandlerAdapter的beanName
         */
        private const val HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter"

        /**
         *  默认的HandlerMapping的beanName
         */
        private const val HANDLER_MAPPING_BEAN_NAME = "handlerMapping"

        /**
         * 默认的HandlerExceptionHandler的beanName
         */
        private const val HANDLER_EXCEPTION_HANDLER_BEAN_NAME = "handlerExceptionResolver"

        /**
         * 默认的ViewResolver的beanName
         */
        private const val VIEW_RESOLVER_BEAN_BEAN = "viewResolver"

        /**
         * 默认的RequestToViewNameTranslator的beanName
         */
        private const val REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator"
    }

    /**
     * 是否要从Spring BeanFactory当中去探测HandlerAdapter? 
     */
    var detectAllHandlerAdapters = true

    /**
     * 是否要从Spring BeanFactory当中去探测所有的HandlerMapping? 默认为true
     */
    var detectAllHandlerMappings = true

    /**
     * 是否要从Spring BeanFactory当中探测所有的HandlerExceptionResolver? 默认为true
     */
    var detectAllHandlerExceptionHandlers = true

    /**
     * 是否从Spring BeanFactory当中要探测所有的视图解析器? 默认为true
     */
    var detectAllViewResolvers = true

    /**
     * HandlerMapping列表
     */
    @Nullable
    private var handlerMappings: MutableList<HandlerMapping>? = null

    /**
     * HandlerAdapter列表
     */
    @Nullable
    private var handlerAdapters: MutableList<HandlerAdapter>? = null

    /**
     * 处理异常的HandlerExceptionResolver列表
     */
    @Nullable
    private var handlerExceptionResolvers: MutableList<HandlerExceptionResolver>? = null

    /**
     * 视图解析器列表, 负责完成视图的解析(根据viewName去获取到对应的View视图对象); 
     * 对于模板引擎的视图渲染, 就会使用到[ViewResolver]去进行解析视图, 将viewName去转换成为对应的View视图对象
     */
    @Nullable
    private var viewResolvers: MutableList<ViewResolver>? = null

    /**
     * ApplicationContext
     */
    @Nullable
    private var applicationContext: ApplicationContext? = null

    /**
     * 视图名的翻译器, 在方法的返回值解析器没有找到合适的视图名时, 它需要从请求当中去获取到对应的视图名
     */
    @Nullable
    private var viewNameTranslator: RequestToViewNameTranslator? = null

    override fun doDispatch(request: HttpServerRequest, response: HttpServerResponse) {
        var mappedHandler: HandlerExecutionChain? = null

        // 根据request去获取到WebAsyncManager
        val asyncManager = WebAsyncUtils.getAsyncManager(request)
        try {
            var dispatchException: Throwable? = null
            var mv: ModelAndView? = null
            try {
                // 遍历所有的HandlerMapping获取HandlerExecutionChain去处理本次请求
                mappedHandler = getHandler(request)

                // 如果没有找到合适的Handler, 得处理404的情况...
                if (mappedHandler == null) {
                    notHandlerFound(request, response)
                    return
                }

                // 获取到处理请求的合适的HandlerAdapter
                val handlerAdapter = getHandlerAdapter(mappedHandler.getHandler())

                // 交给所有的HandlerInterceptor去拦截本次请求去进行处理
                if (!mappedHandler.applyPreHandle(request, response)) {
                    return
                }

                // 真正地去执行Handler, 交给HandlerAdapter去解析参数、执行目标方法、处理返回值
                mv = handlerAdapter.handle(request, response, mappedHandler.getHandler())

                // 如果并发处理任务已经启动了, 那么就直接return, 不需要去进行向后的处理工作了...
                if (asyncManager.isConcurrentHandlingStarted()) {
                    return
                }

                // 如果必要的话, 需要使用视图名翻译器, 将请求路径直接翻译成为视图名...
                // 如果解析出来了ModelAndView, 但是没有找到合适的viewName, 此时就需要用到ViewNameTranslator
                applyDefaultViewName(request, mv)

                // 逆方向去执行拦截器链的所有的postHandle方法
                mappedHandler.applyPostHandle(request, response)
            } catch (ex: Throwable) {
                dispatchException = ex
                logger.error("处理请求[uri=${request.getUri()}]失败", ex)
            }

            // 处理dispatch派发的结果, 处理异常以及渲染视图...
            processDispatchResult(request, response, mappedHandler, mv, dispatchException)
        } catch (ex: Throwable) {
            logger.error("处理请求[uri=${request.getUri()}]失败, 原因是[$ex]", ex)
            throw ex
        }

    }

    /**
     * 处理派发的结果, 如果是异常情况的话, 需要使用异常解析器去解析异常, 如果不是异常情况的话, 要去进行视图的渲染
     *
     * @param request request
     * @param response response
     * @param mappedHandler HandlerExecutionChain
     * @param mv ModelAndView
     * @param ex 处理请求过程当中发生的异常(如果有的话)
     */
    private fun processDispatchResult(
        request: HttpServerRequest,
        response: HttpServerResponse,
        @Nullable mappedHandler: HandlerExecutionChain?,
        @Nullable mv: ModelAndView?,
        @Nullable ex: Throwable?
    ) {
        var modelAndView = mv
        // 如果出现了异常的话交给ExceptionResolver去进行处理...尝试去进行视图的解析工作
        if (ex != null) {
            modelAndView = processHandlerException(request, response, mappedHandler?.getHandler(), ex)
        }

        // 如果必要的话(ModelAndView不为空), 需要去进行进行视图的渲染
        if (modelAndView != null) {
            render(request, response, modelAndView)
        }

        // 使用拦截器链条去触发收尾工作...
        mappedHandler?.triggerAfterCompletion(request, response, ex)
    }

    /**
     * 执行真正的渲染视图
     *
     * @param request request
     * @param response response
     * @param mv ModelAndView
     */
    protected open fun render(request: HttpServerRequest, response: HttpServerResponse, mv: ModelAndView) {
        val viewName = mv.getViewName()

        // 如果是viewName的话, 需要使用ViewResolver, 去将一个viewName去转换成为一个View对象
        val view = if (viewName != null) resolveView(viewName, mv.modelMap, request) else mv.getView()

        // 使用View的render方法去执行渲染视图
        view?.render(mv.modelMap, request, response)
    }

    /**
     * 遍历所有的视图解析器, 尝试去进行视图的解析
     *
     * @param viewName viewName(视图名)
     * @param model model数据
     * @param request request
     * @return 解析到的视图对象(没有解析到合适的View的话, return null), 后续过程当中可以调用render方法去进行渲染
     */
    @Nullable
    protected open fun resolveView(viewName: String, @Nullable model: ModelMap?, request: HttpServerRequest): View? {
        this.viewResolvers?.forEach {
            val view = it.resolveViewName(viewName)
            if (view != null) {
                return view
            }
        }
        return null
    }

    /**
     * 如果[ModelAndView]当中还没有解析到对应的视图(View)的话, 需要去应用默认的视图名
     *
     * @param request request
     * @param mav ModelAndView
     */
    protected open fun applyDefaultViewName(request: HttpServerRequest, @Nullable mav: ModelAndView?) {
        if (mav != null && !mav.hasView()) {
            val defaultViewName = getDefaultViewName(request)
            if (defaultViewName != null) {
                mav.setViewName(defaultViewName)
            }
        }
    }

    /**
     * 使用viewNameTranslator去获取默认的视图名
     *
     * @param request request
     * @return 如果获取到了默认的视图名的话, 那么return 获取到的视图名; 不然return null
     */
    @Nullable
    protected open fun getDefaultViewName(request: HttpServerRequest): String? =
        viewNameTranslator?.getViewName(request)

    /**
     * 交给HandlerExceptionResolver去进行处理HandlerException
     *
     * @param request request
     * @param response response
     * @param handler handler
     * @param ex 派发过程当中的异常
     * @return 处理异常得到的ModelAndView(可以为null, 代表不去渲染视图)
     */
    @Nullable
    protected open fun processHandlerException(
        request: HttpServerRequest, response: HttpServerResponse, @Nullable handler: Any?, ex: Throwable
    ): ModelAndView? {
        var modelAndView: ModelAndView? = null
        val exceptionResolvers = this.handlerExceptionResolvers ?: return null
        // 遍历所有的ExceptionResolver, 去进行异常的解析(例如解析@ExceptionHandler方法的Resolver)
        for (resolver in exceptionResolvers) {
            // 如果返回一个非空的ModelAndView, 终止后续HandlerExceptionResolver的执行...
            modelAndView = resolver.resolveException(request, response, handler, ex)
            if (modelAndView != null) {
                break
            }
        }

        // 如果返回了视图的话...那么需要去进行解析...
        if (modelAndView != null) {
            // 如果是个空的ModelAndView, 那么return null(比如@ResponseBody这种已经被处理过的情况)
            if (modelAndView.isEmpty()) {
                return null
            }
            // 如果没有解析到modelAndView的话...那么这里也得设置默认的viewName
            if (!modelAndView.hasView()) {
                modelAndView = ModelAndView()
                modelAndView.view = "/hello"
            }
        }
        return null
    }

    /**
     * 处理没有找到Handler去处理当前mapping的情况(也就是404的情况)
     *
     * @param request request
     * @param response response
     */
    protected open fun notHandlerFound(request: HttpServerRequest, response: HttpServerResponse) {
        if (logger.isWarnEnabled) {
            logger.warn("[NOT-FOUND]--没有找到合适的Handler去处理本次请求[path=${request.getUri()}, method=${request.getMethod()}, headers=[${request.getHeaders()}]]")
        }
        // sendError(404)
        response.sendError(HttpServerResponse.SC_NOT_FOUND)
    }

    /**
     * 遍历已经注册到当前的DispatcherHandler当中的所有的HandlerAdapter, 去找到合适的一个去处理本次请求
     *
     * @param handler handler
     * @return 如果找到了合适的Adapter来处理请求的话, return HandlerAdapter
     * @throws IllegalStateException 如果没有找到合适的Handler去进行处理的话
     */
    protected open fun getHandlerAdapter(handler: Any): HandlerAdapter {
        this.handlerAdapters?.forEach {
            if (it.supports(handler)) {
                return it
            }
        }
        throw IllegalStateException("没有为[handler=$handler]找到合适的HandlerAdapter去处理")
    }

    /**
     * 遍历所有的HandlerMapping, 去找到合适的HandlerExecutionChain去处理本次请求
     *
     * @param request request
     * @return 如果找到了合适的Handler, 那么return HandlerExecutionChain; 否则, return null
     */
    @Nullable
    protected open fun getHandler(request: HttpServerRequest): HandlerExecutionChain? {
        this.handlerMappings?.forEach {
            val handler = it.getHandler(request)
            if (handler != null) {
                return handler
            }
        }
        return null
    }

    /**
     * 完成SpringMVC当中各个核心组件的初始化工作
     *
     * @param applicationContext ApplicationContext
     */
    open fun onRefresh(applicationContext: ApplicationContext) {
        // 初始化所有的HandlerMapping
        initHandlerMappings(applicationContext)

        // 初始化所有的HandlerAdapter
        initHandlerAdapters(applicationContext)

        // 初始化所有的HandlerExceptionResolver
        initHandlerExceptionResolvers(applicationContext)

        // 初始化所有的视图解析器
        initViewResolvers(applicationContext)

        // 初始化视图名的翻译器
        initViewNameTranslator(applicationContext)
    }

    /**
     * 获取HandlerMapping列表
     *
     * @return 当前DispatcherHandler当中的HandlerMapping列表
     */
    @Nullable
    override fun getHandlerMappings(): List<HandlerMapping>? {
        return if (this.handlerMappings == null) null else ArrayList(this.handlerMappings!!)
    }

    /**
     * 获取HandlerAdapter列表
     *
     * @return  当前DispatcherHandler当中的HandlerAdapter列表
     */
    @Nullable
    override fun getHandlerAdapters(): List<HandlerAdapter>? {
        return if (this.handlerAdapters == null) null else ArrayList(handlerAdapters!!)
    }

    /**
     * 初始化[ApplicationContext], 向[ApplicationContext]中去加入一个[ContextRefreshListener],
     * 从而去初始化当前[DispatcherHandler]当中的各个组件
     */
    private fun initWebApplicationContext() {
        val applicationContext = this.applicationContext
        if (applicationContext is ConfigurableApplicationContext) {
            // 添加监听容器刷新完成的监听器, 完成组件的初始化
            applicationContext.addApplicationListener(ContextRefreshListener())
        }
    }

    /**
     * setApplicationContext
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext

        // 初始化ApplicationContext, 添加ContextRefreshListener, 去完成内部的核心组件的初始化工作
        initWebApplicationContext()
    }

    open fun getApplicationContext(): ApplicationContext? = this.applicationContext

    /**
     * 初始化ViewName的翻译器
     *
     * * 1.尝试从ApplicationContext根据beanName去进行获取
     * * 2.ApplicationContext当中没有, 那么从配置文件当中去进行加载
     *
     * @param applicationContext ApplicationContext
     */
    private fun initViewNameTranslator(applicationContext: ApplicationContext) {
        try {
            this.viewNameTranslator = applicationContext.getBean(
                REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator::class.java
            )
        } catch (ex: NoSuchBeanDefinitionException) {
            this.viewNameTranslator =
                getDefaultStrategies(applicationContext, RequestToViewNameTranslator::class.java)[0]
        }
    }

    /**
     * 初始化HandlerMapping, 从容器当中拿出所有的HandlerMapping
     *
     * @param applicationContext ApplicationContext
     */
    private fun initHandlerMappings(applicationContext: ApplicationContext) {
        val handlerMappings = ArrayList<HandlerMapping>()
        if (detectAllHandlerMappings) {
            handlerMappings += applicationContext.getBeansForTypeIncludingAncestors(HandlerMapping::class.java).values
            AnnotationAwareOrderComparator.sort(handlerMappings)
        } else {
            try {
                handlerMappings += applicationContext.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping::class.java)
            } catch (ignored: NoSuchBeanDefinitionException) {
                // ignored
            }
        }
        // 如果还为空, 从配置文件当中去加载默认的策略...
        if (handlerMappings.isEmpty()) {
            handlerMappings += getDefaultStrategies(applicationContext, HandlerMapping::class.java)
        }
        this.handlerMappings = handlerMappings
    }

    /**
     * 初始化HandlerAdapter
     *
     * @param applicationContext ApplicationContext
     */
    private fun initHandlerAdapters(applicationContext: ApplicationContext) {
        val handlerAdapters = ArrayList<HandlerAdapter>()
        if (detectAllHandlerAdapters) {
            handlerAdapters += applicationContext.getBeansForTypeIncludingAncestors(HandlerAdapter::class.java).values
            AnnotationAwareOrderComparator.sort(handlerAdapters)
        } else {
            try {
                handlerAdapters += applicationContext.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter::class.java)
            } catch (ignored: NoSuchBeanDefinitionException) {
                // ignored
            }
        }
        // 如果还为空, 从配置文件当中去加载默认的策略...
        if (handlerAdapters.isEmpty()) {
            handlerAdapters += getDefaultStrategies(applicationContext, HandlerAdapter::class.java)
        }
        this.handlerAdapters = handlerAdapters
    }

    /**
     * 初始化HandlerExceptionResolver
     *
     * @param applicationContext ApplicationContext
     */
    private fun initHandlerExceptionResolvers(applicationContext: ApplicationContext) {
        val handlerExceptionResolvers = ArrayList<HandlerExceptionResolver>()
        if (detectAllHandlerExceptionHandlers) {
            handlerExceptionResolvers += applicationContext.getBeansForTypeIncludingAncestors(HandlerExceptionResolver::class.java).values
            AnnotationAwareOrderComparator.sort(handlerExceptionResolvers)
        } else {
            try {
                handlerExceptionResolvers += applicationContext.getBean(
                    HANDLER_EXCEPTION_HANDLER_BEAN_NAME, HandlerExceptionResolver::class.java
                )
            } catch (ignored: NoSuchBeanDefinitionException) {
                // ignored
            }
        }
        // 如果还为空, 从配置文件当中去加载默认的策略...
        if (handlerExceptionResolvers.isEmpty()) {
            handlerExceptionResolvers += getDefaultStrategies(applicationContext, HandlerExceptionResolver::class.java)
        }
        this.handlerExceptionResolvers = handlerExceptionResolvers
    }

    /**
     * 初始化ViewResolver
     *
     * @param applicationContext ApplicationContext
     */
    private fun initViewResolvers(applicationContext: ApplicationContext) {
        val viewResolvers = ArrayList<ViewResolver>()
        if (detectAllViewResolvers) {
            viewResolvers += applicationContext.getBeansForTypeIncludingAncestors(ViewResolver::class.java).values
            AnnotationAwareOrderComparator.sort(viewResolvers)
        } else {
            try {
                viewResolvers += applicationContext.getBean(VIEW_RESOLVER_BEAN_BEAN, ViewResolver::class.java)
            } catch (ignored: NoSuchBeanDefinitionException) {
                // ignored
            }
        }
        this.viewResolvers = viewResolvers
    }

    /**
     * 给定具体的策略接口, 从配置文件当中获取默认的策略并交给ApplicationContext去进行初始化工作
     *
     * @param applicationContext ApplicationContext
     * @param strategyInterface 策略接口(HandlerMapping/HandlerAdapter/HandlerExceptionResolver等)
     */
    private fun <T> getDefaultStrategies(applicationContext: ApplicationContext, strategyInterface: Class<T>): List<T> {
        val result = ArrayList<T>()
        val properties = PropertiesLoaderUtils.loadAllProperties(DEFAULT_STRATEGIES_PATH)
        // 获取到该策略接口对应的实现类列表
        val property = (properties[strategyInterface.name] ?: "").toString()
        // 获取该策略接口所配置的全部实现类
        val classNames = StringUtils.commaDelimitedListToStringArray(property)
        classNames.forEach {
            try {
                val impl = ClassUtils.forName<T>(it, DispatcherHandler::class.java.classLoader)
                result += applicationContext.getAutowireCapableBeanFactory().createBean(impl)
            } catch (ex: Exception) {
                throw BeansException("在DispatcherHandler的初始化过程当中, 无法找到类[$it]")
            }
        }
        if (classNames.isEmpty()) {
            if (logger.isTraceEnabled) {
                logger.info("没有从容器当中找到[${strategyInterface.name}]的具体实现, 从配置文件当中加载到如下实现[$property]")
            }
        }
        return result
    }

    /**
     * 监听Context刷新完成的事件, 负责完成DispatcherHandler当中的各个核心组件的初始化工作
     *
     * Note: 这里需要使用inner内部类, 方便去获取外部类的方法
     */
    inner class ContextRefreshListener : SmartApplicationListener {
        /**
         * 处理事件的方式是, 交给DispatcherHandler.OnRefresh方法去进行组件的初始化
         *
         * @param event event
         */
        override fun onApplicationEvent(event: ApplicationEvent) = onRefresh(event.source as ApplicationContext)

        /**
         * 支持去处理的事件类型为[ContextRefreshedEvent]
         *
         * @param eventType eventType
         */
        override fun supportEventType(eventType: Class<out ApplicationEvent>) =
            eventType == ContextRefreshedEvent::class.java
    }
}