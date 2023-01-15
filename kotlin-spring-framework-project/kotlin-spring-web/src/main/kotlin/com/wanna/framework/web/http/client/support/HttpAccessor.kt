package com.wanna.framework.web.http.client.support

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.client.ClientHttpRequest
import com.wanna.framework.web.http.client.ClientHttpRequestFactory
import java.net.URI
import com.wanna.framework.web.client.RestTemplate
import com.wanna.framework.web.http.client.HttpComponentsClientHttpRequestFactory

/**
 * 它是一个基础的HttpAccessor, 提供Http访问的入口, 不要直接使用, 具体的使用见RestTemplate
 *
 * @see RestTemplate
 */
abstract class HttpAccessor {

    // ClientHttpRequestFactory, 默认为Apache的HttpClientFactory, 去创建一个Apache的HttpClient的RequestFactory
    private var requestFactory: ClientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()

    open fun getRequestFactory(): ClientHttpRequestFactory {
        return this.requestFactory
    }

    open fun setRequestFactory(requestFactory: ClientHttpRequestFactory) {
        this.requestFactory = requestFactory
    }

    protected open fun createRequest(url: URI, method: RequestMethod): ClientHttpRequest {
        return getRequestFactory().createRequest(url, method)
    }
}