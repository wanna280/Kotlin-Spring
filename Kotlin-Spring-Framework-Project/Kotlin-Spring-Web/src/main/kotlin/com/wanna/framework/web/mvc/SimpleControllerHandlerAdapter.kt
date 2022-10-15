package com.wanna.framework.web.mvc

import com.wanna.framework.web.handler.HandlerAdapter
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 简单的Controller的HandlerAdapter，负责处理HandlerMapping返回值的Handler是一个Controller的情况；
 *
 * 它和HttpRequestHandlerAdapter类似，直接是使用对应的Controller(HttpRequestHandler)去进行处理请求；
 * 区别在于HttpRequestHandler返回值的是Unit(Void)，而Controller返回的是一个ModelAndView
 *
 * @see Controller
 * @see HttpRequestHandlerAdapter
 */
open class SimpleControllerHandlerAdapter : HandlerAdapter {
    override fun supports(handler: Any) = handler is Controller
    override fun handle(request: HttpServerRequest, response: HttpServerResponse, handler: Any): ModelAndView? {
        return (handler as Controller).handleRequest(request, response)
    }
}