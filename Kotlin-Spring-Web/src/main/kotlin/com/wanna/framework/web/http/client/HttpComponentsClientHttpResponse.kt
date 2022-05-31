package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpHeaders
import java.io.InputStream

/**
 * 基于Apache的HttpComponents/HttpClient实现的HttpResponse
 *
 * @see ClientHttpRequest
 * @see HttpComponentsClientHttpRequest
 */
class HttpComponentsClientHttpResponse : ClientHttpResponse {

    private val headers = HttpHeaders()

    private var body: InputStream? = null

    fun setBody(body: InputStream) {
        this.body = body
    }

    override fun getBody(): InputStream {
        return body!!
    }

    override fun getHeaders(): HttpHeaders {
        return headers
    }
}