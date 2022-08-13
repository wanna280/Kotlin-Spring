package com.wanna.framework.web.handler

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * SpringMVC的超级反射工具，用来去反射调用目标Handler方法，去处理请求
 */
interface HandlerAdapter {

    /**
     * 是否支持去处理这样的Handler？
     *
     * @param handler handler对象
     * @return 如果支持处理Handler的情况，那么return true；如果不支持则return false
     */
    fun supports(handler: Any): Boolean

    /**
     * 如果支持去进行处理的话，那么交给当前的HandlerAdapter去进行处理当前的请求
     *
     * @param request request
     * @param response response
     * @param handler handler
     * @return Handler处理的结果，ModelAndView(封装了模型和数据)
     */
    fun handle(request: HttpServerRequest, response: HttpServerResponse, handler: Any) : ModelAndView?
}