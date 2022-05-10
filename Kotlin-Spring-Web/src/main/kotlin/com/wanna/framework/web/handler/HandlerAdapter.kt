package com.wanna.framework.web.handler

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 超级反射工具，用来去反射调用目标Handler方法，去处理请求
 */
interface HandlerAdapter {

    /**
     * 是否支持去处理这样的Handler？
     */
    fun supports(handler: Any): Boolean

    /**
     * 如果支持去进行处理的话，那么交给当前的HandlerAdapter去进行处理当前的请求
     *
     * @param request request
     * @param response response
     * @param handler handler
     */
    fun handle(request: HttpServerRequest, response: HttpServerResponse, handler: Any) : ModelAndView?
}