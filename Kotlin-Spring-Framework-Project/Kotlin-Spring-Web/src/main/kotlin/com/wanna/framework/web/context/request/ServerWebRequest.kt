package com.wanna.framework.web.context.request

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 它是NativeWebRequest的实现，使用HttpServerRequest作为request，使用HttpServerResponse作为response
 */
open class ServerWebRequest(private val request: HttpServerRequest, private val response: HttpServerResponse? = null) :
    NativeWebRequest {

    override fun getNativeRequest(): Any {
        return request
    }

    override fun getNativeResponse(): Any? {
        return response
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getNativeRequest(type: Class<T>): T {
        return request as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getNativeResponse(type: Class<T>): T? {
        return response as T?
    }
}