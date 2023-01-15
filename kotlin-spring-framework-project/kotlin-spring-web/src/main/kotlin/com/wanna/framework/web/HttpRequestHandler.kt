package com.wanna.framework.web

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.framework.web.mvc.Controller


/**
 * HttpRequestHandler, 直接处理目标请求, 需要将响应的数据直接写入到response当中
 *
 * @see Controller
 */
@FunctionalInterface
interface HttpRequestHandler {
    fun handleRequest(request: HttpServerRequest, response: HttpServerResponse)
}