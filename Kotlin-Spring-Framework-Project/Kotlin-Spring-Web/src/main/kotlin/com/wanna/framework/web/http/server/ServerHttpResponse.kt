package com.wanna.framework.web.http.server

import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.server.HttpServerResponse
import java.io.OutputStream

/**
 * ServerHttpResponse
 */
open class ServerHttpResponse(webRequest: NativeWebRequest) : HttpOutputMessage {

    private val response = webRequest.getNativeResponse(HttpServerResponse::class.java)

    override fun getHeaders(): HttpHeaders {
        return this.response!!.getHeaders()
    }

    override fun getBody(): OutputStream {
        return this.response!!.getOutputStream()
    }
}