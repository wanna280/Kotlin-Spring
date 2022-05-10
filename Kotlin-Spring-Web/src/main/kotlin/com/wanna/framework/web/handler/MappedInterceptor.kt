package com.wanna.framework.web.handler

import com.wanna.framework.web.HandlerInterceptor
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

class MappedInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServerRequest, response: HttpServerResponse, handler: Any): Boolean {
        return true
    }

    override fun postHandle(request: HttpServerRequest, response: HttpServerResponse, handler: Any) {

    }

    override fun afterCompletion(
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: Any,
        ex: Throwable?
    ) {

    }
}