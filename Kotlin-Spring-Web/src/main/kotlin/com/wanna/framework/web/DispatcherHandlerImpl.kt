package com.wanna.framework.web

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.ContextRefreshedEvent
import com.wanna.framework.context.event.SmartApplicationListener
import com.wanna.framework.web.handler.HandlerAdapter
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * DispatcherHandler的具体实现
 */
open class DispatcherHandlerImpl : DispatcherHandler {

    private val logger: Logger = LoggerFactory.getLogger(DispatcherHandlerImpl::class.java)

    // handlerMapping列表
    private var handlerMappings: MutableList<HandlerMapping>? = null

    // handlerAdapter列表
    private var handlerAdapters: MutableList<HandlerAdapter>? = null

    // handlerExceptionResolver列表
    private var handlerExceptionResolvers: MutableList<HandlerExceptionResolver>? = null

    // ApplicationContext
    private var applicationContext: ApplicationContext? = null

    override fun doDispatch(request: HttpServerRequest, response: HttpServerResponse) {

        try {
            // 遍历所有的HandlerMapping获取HandlerExecutionChain去处理本次请求
            val mappedHandler = getHandler(request)

            // 如果没有找到合适的Handler，得处理404的情况...
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

            // 真正地去执行Handler，交给HandlerAdapter去解析参数、执行目标方法、处理返回值
            val mv = handlerAdapter.handle(request, response, mappedHandler.getHandler())

            // 逆方向去执行拦截器链的所有的postHandle方法
            mappedHandler.applyPostHandle(request, response)
        } catch (ex: Throwable) {
            logger.error("处理请求失败，原因是[$ex]", ex)
        }
    }

    /**
     * 处理没有找到Handler去处理当前mapping的情况(也就是404的情况)
     *
     * @param request request
     * @param response response
     */
    protected open fun notHandlerFound(request: HttpServerRequest, response: HttpServerResponse) {
        if (logger.isWarnEnabled) {
            logger.warn("没有找到合适的Handler去处理本次请求[path=${request.getUri()}]")
        }
        // sendError(404)
        response.sendError(HttpServerResponse.SC_NOT_FOUND)
    }

    /**
     * 遍历已经注册到当前的DispatcherHandler当中的所有的HandlerAdapter，去找到合适的一个去处理本次请求
     *
     * @param handler handler
     * @return 如果找到了合适的Adapter来处理请求的话，return HandlerAdapter
     * @throws IllegalStateException 如果没有找到合适的Handler去进行处理的话
     */
    protected open fun getHandlerAdapter(handler: Any): HandlerAdapter {
        if (this.handlerAdapters != null) {
            this.handlerAdapters!!.forEach {
                if (it.supports(handler)) {
                    return it
                }
            }
        }
        throw IllegalStateException("没有为[handler=$handler]找到合适的HandlerAdapter去处理")
    }

    /**
     * 遍历所有的HandlerMapping，去找到合适的HandlerExecutionChain去处理本次请求
     *
     * @param request request
     * @return 如果找到了合适的Handler，那么return HandlerExecutionChain；否则，return null
     */
    protected open fun getHandler(request: HttpServerRequest): HandlerExecutionChain? {
        if (this.handlerMappings == null) {
            return null
        }
        this.handlerMappings!!.forEach {
            val handler = it.getHandler(request)
            if (handler != null) {
                return handler
            }
        }
        return null
    }

    open fun onRefresh(applicationContext: ApplicationContext) {
        initHandlerMappings(applicationContext)
        initHandlerAdpater(applicationContext)
    }

    /**
     * 获取HandlerMapping列表
     */
    protected open fun getHandlerMappings(): Collection<HandlerMapping>? {
        return if (this.handlerMappings == null) null else ArrayList(this.handlerMappings!!)
    }

    /**
     * 添加初始化ApplicationContext
     */
    private fun initWebApplicationContext() {
        // 添加监听容器刷新完成的监听器，完成组件的初始化
        (this.applicationContext!! as ConfigurableApplicationContext).addApplicationListener(ContextRefreshListener())
    }

    open fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        initWebApplicationContext()  // 初始化ApplicationContext
    }

    open fun getApplicationContext(): ApplicationContext {
        return this.applicationContext!!
    }

    /**
     * 初始化HandlerMapping，从容器当中拿出所有的HandlerMapping
     */
    private fun initHandlerMappings(applicationContext: ApplicationContext) {
        this.handlerMappings = ArrayList()
        this.handlerMappings!! += applicationContext.getBeansForType(HandlerMapping::class.java).values
    }

    /**
     * 初始化HandlerAdapter
     */
    private fun initHandlerAdpater(applicationContext: ApplicationContext) {
        this.handlerAdapters = ArrayList()
        this.handlerAdapters!! += applicationContext.getBeansForType(HandlerAdapter::class.java).values
    }

    /**
     * 监听Context刷新完成的事件，负责完成各个核心组件的初始化
     */
    inner class ContextRefreshListener : SmartApplicationListener {
        override fun onApplicationEvent(event: ApplicationEvent) {
            this@DispatcherHandlerImpl.onRefresh(event.source as ApplicationContext)
        }

        override fun supportEventType(eventType: Class<out ApplicationEvent>): Boolean {
            return eventType == ContextRefreshedEvent::class.java
        }
    }
}