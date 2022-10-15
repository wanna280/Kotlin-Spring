package com.wanna.boot.devtools.restart.server

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

class HttpRestartServerHandler(private val server: HttpStartServer) {
    fun handle(request: HttpServerRequest, response: HttpServerResponse) {
        server.handle(request, response)
    }
}