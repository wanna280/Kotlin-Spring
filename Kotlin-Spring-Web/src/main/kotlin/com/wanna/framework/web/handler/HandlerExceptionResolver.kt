package com.wanna.framework.web.handler

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * Handler处理过程当中的异常的解析器，负责去处理本次请求当中的异常
 */
interface HandlerExceptionResolver {
    fun resolveException(
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: Any?,
        ex: Throwable
    ): ModelAndView
}