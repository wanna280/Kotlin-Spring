package com.wanna.framework.web.http.server

import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.server.HttpServerRequest
import java.io.InputStream


/**
 * ServerHttpRequest
 */
open class ServerHttpRequest(webRequest: NativeWebRequest) : HttpInputMessage {

    private val request = webRequest.getNativeRequest(HttpServerRequest::class.java)

    fun getRequest() : HttpServerRequest {
        return request
    }

    override fun getBody(): InputStream {
        return request.getInputStream()
    }

    override fun getHeaders(): Map<String, String> {
        return request.getHeaders()
    }
}