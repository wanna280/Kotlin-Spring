package com.wanna.framework.web.mvc

import com.wanna.framework.lang.Nullable
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

    /**
     * 支持去进行处理的Handler类型是Controller类型
     *
     * @see Controller
     * @return 如果handle is Controller, return true; 否则return false
     */
    override fun supports(handler: Any) = handler is Controller

    /**
     * 使用该Controller去处理请求
     *
     * @param request request
     * @param response response
     * @param handler handler
     * @return ModelAndView
     */
    @Nullable
    override fun handle(request: HttpServerRequest, response: HttpServerResponse, handler: Any): ModelAndView? {
        return (handler as Controller).handleRequest(request, response)
    }
}