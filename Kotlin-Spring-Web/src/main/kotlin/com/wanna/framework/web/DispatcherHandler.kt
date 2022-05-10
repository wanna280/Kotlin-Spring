package com.wanna.framework.web

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 处理请求的Handler
 */
interface DispatcherHandler {
    /**
     * 处理(派发)本次请求
     *
     * @param request request
     * @param response response
     */
    fun doDispatch(request: HttpServerRequest, response: HttpServerResponse)
}