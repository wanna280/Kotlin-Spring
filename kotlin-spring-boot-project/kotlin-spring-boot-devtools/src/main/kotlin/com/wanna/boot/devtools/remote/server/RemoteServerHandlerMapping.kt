package com.wanna.boot.devtools.remote.server

import com.wanna.boot.devtools.restart.server.HttpRestartServerHandler
import com.wanna.boot.devtools.restart.server.HttpStartServer
import com.wanna.boot.devtools.restart.server.StartServer
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.method.RequestMappingInfo
import com.wanna.framework.web.method.annotation.RequestMappingHandlerMapping
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * RemoteServer的"DevTools"的HandlerMapping
 */
open class RemoteServerHandlerMapping : RequestMappingHandlerMapping() {

    private val handlerMethod = ReflectionUtils.findMethod(
        Handler::class.java,
        "handle",
        HttpServerRequest::class.java,
        HttpServerResponse::class.java
    )!!

    private val handler = HttpRestartServerHandler(HttpStartServer(StartServer()))

    override fun initHandlerMethods() {
        val mapping = RequestMappingInfo.Builder()
            .paths("/spring-devtools/**")
            .methods(RequestMethod.POST).build()
        registerHandlerMethod(Handler(), this.handlerMethod, mapping)
    }

    inner class Handler {
        fun handle(request: HttpServerRequest, response: HttpServerResponse) {
            handler.handle(request, response)
        }
    }
}