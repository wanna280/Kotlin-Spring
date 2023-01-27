package com.wanna.boot.devtools.restart.server

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 基于HTTP去实现重启的处理器
 *
 * @param server HttpStartServer
 */
class HttpRestartServerHandler(private val server: HttpStartServer) {
    fun handle(request: HttpServerRequest, response: HttpServerResponse) {
        server.handle(request, response)
    }
}