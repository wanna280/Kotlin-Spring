package com.wanna.framework.web.mvc

import com.wanna.framework.web.HttpRequestHandler
import com.wanna.framework.web.handler.HandlerAdapter
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * HttpRequestHandler的HandlerAdapter, 负责处理HandlerMapping返回的handler是HttpRequestHandler的情况
 *
 * 它和SimpleControllerHandlerAdapter类似, 直接是使用对应的Controller(HttpRequestHandler)去进行处理请求;
 * 区别在于HttpRequestHandler返回值的是Unit(Void), 而Controller返回的是一个ModelAndView
 *
 * @see HandlerAdapter
 * @see HttpRequestHandler
 * @see SimpleControllerHandlerAdapter
 */
open class HttpRequestHandlerAdapter : HandlerAdapter {
    override fun supports(handler: Any) = handler is HttpRequestHandler
    override fun handle(request: HttpServerRequest, response: HttpServerResponse, handler: Any): ModelAndView? {
        (handler as HttpRequestHandler).handleRequest(request, response)
        return null
    }
}