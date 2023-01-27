package com.wanna.framework.web.http.client.support

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.client.RestTemplate
import com.wanna.framework.web.http.client.ClientHttpRequest
import com.wanna.framework.web.http.client.ClientHttpRequestFactory
import com.wanna.framework.web.http.client.SimpleClientHttpRequestFactory
import java.net.URI

/**
 * 它是一个基础的HttpAccessor, 提供Http访问的入口, 不要直接使用, 具体的使用见RestTemplate
 *
 * @see RestTemplate
 */
abstract class HttpAccessor {

    /**
     * ClientHttpRequestFactory
     */
    private var requestFactory: ClientHttpRequestFactory = SimpleClientHttpRequestFactory()

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