package com.wanna.framework.web.http.client.support

import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.http.client.ClientHttpRequest
import com.wanna.framework.web.http.client.ClientHttpRequestFactory
import com.wanna.framework.web.http.client.HttpComponentsClientHttpRequest
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import java.io.ByteArrayOutputStream
import java.net.URI
import com.wanna.framework.web.client.RestTemplate

/**
 * 它是一个基础的HttpAccessor，提供Http访问的入口，不要直接使用，具体的使用见RestTemplate
 *
 * @see RestTemplate
 */
abstract class HttpAccessor {

    private var requestFactory: ClientHttpRequestFactory = object : ClientHttpRequestFactory {
        override fun create(url: URI, method: RequestMethod): ClientHttpRequest {
            val httpClient = HttpClients.createDefault()
            val httpRequest = when (method) {
                RequestMethod.GET -> HttpGet(url)
                RequestMethod.POST -> HttpPost(url)
                RequestMethod.DELETE -> HttpDelete(url)
                RequestMethod.HEAD -> HttpHead(url)
                else -> throw IllegalStateException("不支持这种请求方式!!!")
            }
            val clientHttpRequest = HttpComponentsClientHttpRequest(httpClient, httpRequest)
            clientHttpRequest.setBody(ByteArrayOutputStream())
            return clientHttpRequest
        }
    }

    open fun getRequestFactory(): ClientHttpRequestFactory {
        return this.requestFactory
    }

    open fun setRequestFactory(requestFactory: ClientHttpRequestFactory) {
        this.requestFactory = requestFactory
    }

    protected open fun createRequest(url: URI, method: RequestMethod): ClientHttpRequest {
        return getRequestFactory().create(url, method)
    }
}