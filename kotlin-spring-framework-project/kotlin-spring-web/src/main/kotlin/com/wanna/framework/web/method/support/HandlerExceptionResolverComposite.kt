package com.wanna.framework.web.method.support

import com.wanna.framework.core.Ordered
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * HandlerExceptionResolver的聚合，它支持遍历所有内部聚合的HandlerExceptionResolver，协作去完成异常的解析
 *
 * @see HandlerExceptionResolver
 */
open class HandlerExceptionResolverComposite : HandlerExceptionResolver, Ordered {

    /**
     * order
     */
    private var order: Int = Ordered.ORDER_LOWEST

    override fun getOrder(): Int {
        return order
    }

    open fun setOrder(order: Int) {
        this.order = order
    }

    @Nullable
    private var resolvers: List<HandlerExceptionResolver>? = null

    /**
     * 设置处理异常的[HandlerExceptionResolver]
     *
     * @param resolvers 需要使用的[HandlerExceptionResolver]列表
     */
    open fun setHandlerExceptionResolver(resolvers: List<HandlerExceptionResolver>) {
        this.resolvers = resolvers
    }

    override fun resolveException(
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: Any?,
        ex: Throwable
    ): ModelAndView? {

        // 利用所有的HandlerExceptionResolver去解析出现的异常信息
        this.resolvers?.forEach {
            val mav = it.resolveException(request, response, handler, ex)

            // 如果其中一个HandlerExceptionResolver解析出来了ModelAndView, 那么直接return null
            if (mav != null) {
                return mav
            }
        }
        return null
    }
}