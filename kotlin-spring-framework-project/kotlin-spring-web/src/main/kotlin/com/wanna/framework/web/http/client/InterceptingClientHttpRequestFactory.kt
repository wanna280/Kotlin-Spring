package com.wanna.framework.web.http.client

import com.wanna.framework.web.bind.annotation.RequestMethod
import java.net.URI

/**
 * 带有拦截功能的ClientHttpRequestFactory, 内部聚合了拦截器列表, 可以对请求去进行自定义
 *
 * Note: 实现基于注册发现的负载均衡时, 就可以拦截request的创建, 去替换掉serviceName的url成为真正的url
 *
 * @param requestFactory RequestFactory
 * @param interceptors 拦截器列表
 *
 * @see InterceptingClientHttpRequest
 */
open class InterceptingClientHttpRequestFactory(
    private val requestFactory: ClientHttpRequestFactory,
    private val interceptors: List<ClientHttpRequestInterceptor>
) : ClientHttpRequestFactory {

    /**
     * 创建带有拦截功能的处理的[ClientHttpRequest]
     *
     * @param uri URI
     * @param method HTTP请求方式
     * @return 带有拦截功能的[ClientHttpRequest]
     */
    override fun createRequest(uri: URI, method: RequestMethod): ClientHttpRequest {
        return createRequest(uri, method, interceptors, requestFactory)
    }

    /**
     * 创建带有拦截功能的[ClientHttpRequest]
     *
     * @param uri URI
     * @param method HTTP请求方式
     * @param interceptors 要使用的拦截器列表
     * @param requestFactory RequestFactory
     *
     * @see InterceptingClientHttpRequest
     *
     */
    open fun createRequest(
        uri: URI,
        method: RequestMethod,
        interceptors: List<ClientHttpRequestInterceptor>,
        requestFactory: ClientHttpRequestFactory
    ): ClientHttpRequest {
        return InterceptingClientHttpRequest(requestFactory, uri, method, interceptors)
    }
}