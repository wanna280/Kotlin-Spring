package com.wanna.framework.web.http.server

import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.server.HttpServerResponse
import java.io.OutputStream

/**
 * ServerHttpResponse
 */
open class ServerHttpResponse(webRequest: NativeWebRequest) : HttpOutputMessage {

    private val response = webRequest.getNativeResponse(HttpServerResponse::class.java)

    private val headers = HashMap<String, String>()

    override fun getHeaders(): Map<String, String> {
        return this.headers
    }

    override fun getBody(): OutputStream {
        return this.response!!.getOutputStream()
    }
}