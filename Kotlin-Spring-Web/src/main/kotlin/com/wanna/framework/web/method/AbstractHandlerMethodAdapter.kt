package com.wanna.framework.web.method

import com.wanna.framework.core.Ordered
import com.wanna.framework.web.handler.HandlerAdapter
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 这是一个抽象的HandlerMethod的HandlerAdapter，它负责去处理Handler类型的HandlerMethod类型的Handler；
 *
 * @see HandlerAdapter
 */
abstract class AbstractHandlerMethodAdapter : HandlerAdapter, Ordered {

    private var order = Ordered.ORDER_LOWEST

    override fun getOrder(): Int {
        return this.order
    }

    fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * 只要handler是HandlerMethod，那么就支持去进行处理本次请求
     *
     * @param handler hanler
     * @return 如果handler是HandlerMethod类型，return true；不然return false
     */
    override fun supports(handler: Any): Boolean {
        return handler is HandlerMethod && supportInternal(handler)
    }

    /**
     * 处理目标请求，将handler限制为HandlerMethod，交给子类去进行实现
     *
     * @param request request
     * @param response response
     * @param handler handlerMethod Object
     */
    override fun handle(request: HttpServerRequest, response: HttpServerResponse, handler: Any): ModelAndView? {
        return handleInternal(request, response, handler as HandlerMethod)
    }

    /**
     * 处理目标请求的具体实现逻辑，交给子类去进行实现
     */
    abstract fun handleInternal(
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: HandlerMethod
    ): ModelAndView?

    /**
     * 子类需要自定义的是否支持处理的方式
     *
     * @param handler HandlerMethod
     * @return 是否支持使用这样的Handler去处理请求？
     */
    abstract fun supportInternal(handler: HandlerMethod): Boolean
}