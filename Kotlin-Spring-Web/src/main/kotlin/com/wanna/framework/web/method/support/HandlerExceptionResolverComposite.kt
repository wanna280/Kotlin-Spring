package com.wanna.framework.web.method.support

import com.wanna.framework.core.Ordered
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * HandlerExceptionResolver的聚合，它支持遍历所有内部聚合的HandlerExceptionResolver，协作去完成异常的解析
 */
open class HandlerExceptionResolverComposite : HandlerExceptionResolver, Ordered {

    private var order: Int = Ordered.ORDER_LOWEST

    override fun getOrder(): Int {
        return order
    }

    open fun setOrder(order: Int) {
        this.order = order
    }

    private var resolvers: List<HandlerExceptionResolver>? = null

    open fun setHandlerExceptionResolver(resolvers: List<HandlerExceptionResolver>) {
        this.resolvers = resolvers
    }

    override fun resolveException(
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: Any?,
        ex: Throwable
    ): ModelAndView? {
        this.resolvers?.forEach {
            val mav = it.resolveException(request, response, handler, ex)
            if (mav != null) {
                return mav
            }
        }
        return null
    }
}