package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpHeaders
import org.apache.http.HttpResponse
import java.io.InputStream

/**
 * 基于Apache的HttpComponents/HttpClient实现的HttpResponse
 *
 * @see ClientHttpRequest
 * @see HttpComponentsClientHttpRequest
 */
internal class HttpComponentsClientHttpResponse(private val response: HttpResponse) : ClientHttpResponse {

    override fun getBody(): InputStream = response.entity.content

    override fun getStatusCode() = response.statusLine.statusCode

    override fun getHeaders(): HttpHeaders {
        val httpHeaders = HttpHeaders()
        response.allHeaders.forEach {
            httpHeaders.add(it.name, it.value)
        }
        return httpHeaders
    }

    override fun close() {

    }
}