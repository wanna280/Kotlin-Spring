package com.wanna.framework.web.server

import java.io.ByteArrayOutputStream

/**
 * Response的OutputStream
 *
 * @param response response
 */
open class ResponseOutputStream(val response: HttpServerResponse, size: Int = 32) : ByteArrayOutputStream(size) {
    override fun flush() {
        response.flush()
    }
}