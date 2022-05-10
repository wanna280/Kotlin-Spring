package com.wanna.framework.web.server

import java.io.ByteArrayOutputStream
import java.io.OutputStream

class HttpServerResponse {
    private val outputStream = ByteArrayOutputStream(1024)

    fun getOutputStream(): OutputStream {
        return outputStream
    }
}