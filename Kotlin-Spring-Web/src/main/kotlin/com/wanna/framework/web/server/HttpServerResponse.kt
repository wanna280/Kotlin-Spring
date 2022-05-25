package com.wanna.framework.web.server

import java.io.ByteArrayOutputStream
import java.io.OutputStream

open class HttpServerResponse {
    companion object {
        const val SC_NOT_FOUND = 404
    }

    private var statusCode: Int = 200 // 响应状态码，默认为200

    private var message: String = ""  // message

    private val outputStream = ByteArrayOutputStream(1024)

    private val headers: MutableMap<String, String> = HashMap()

    open fun getHeaders(): Map<String, String> {
        return headers
    }

    open fun removeHeader(name: String) : String? {
        return this.headers.remove(name)
    }

    open fun setHeader(name: String, value: String?) {
        if (value != null) {
            this.headers[name] = value
        } else {
            this.headers -= name
        }
    }

    open fun getHeader(name: String): String? {
        return headers[name]
    }

    open fun getContentType(): String {
        return headers["Content-Type"] ?: "application/json"
    }

    open fun getStatusCode(): Int {
        return statusCode
    }

    open fun getMessage(): String {
        return message
    }

    open fun getOutputStream(): OutputStream {
        return outputStream
    }

    /**
     * sendError，msg采用默认的msg
     *
     * @param statusCode 状态码
     */
    open fun sendError(statusCode: Int) {
        sendError(statusCode, "")
    }

    /**
     * sendError
     *
     * @param statusCode 状态码
     * @param msg message
     */
    open fun sendError(statusCode: Int, msg: String) {
        this.statusCode = statusCode
        this.message = msg
    }
}