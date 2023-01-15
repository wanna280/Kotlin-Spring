package com.wanna.framework.web.mvc

import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.framework.web.HttpRequestHandler

/**
 * Controller处理目标请求, 并直接返回一个ModelAndView, 交给子类去进行渲染
 *
 * @see HttpRequestHandler
 */
@FunctionalInterface
interface Controller {
    fun handleRequest(request: HttpServerRequest, response: HttpServerResponse): ModelAndView?
}