package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpHeaders
import java.io.ByteArrayOutputStream


/**
 * 带有缓冲的ClientHttpRequest
 */
abstract class AbstractBufferingClientHttpRequest : AbstractClientHttpRequest() {
    private val bufferedOutput = ByteArrayOutputStream(1024)
    override fun getBody() = bufferedOutput

    override fun executeInternal(headers: HttpHeaders): ClientHttpResponse {
        return executeInternal(headers, getBody().toByteArray())
    }

    /**
     * 为子类提供Headers和BufferedOutput的访问
     *
     * @param headers headers
     * @param bufferedOutput bufferedOutput(ByteArray)
     */
    abstract fun executeInternal(headers: HttpHeaders, bufferedOutput: ByteArray): ClientHttpResponse
}