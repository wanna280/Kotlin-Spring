package com.wanna.framework.web.resource

import com.wanna.framework.web.HttpRequestHandler
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * Spring的资源的HttpRequestHandler，负责返回资源(html/css/js/jpg/png等)给客户端
 *
 * @see HttpRequestHandler
 */
open class ResourceHttpRequestHandler : HttpRequestHandler {
    override fun handleRequest(request: HttpServerRequest, response: HttpServerResponse) {

    }
}