package com.wanna.framework.web.http.client.support

import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.client.ClientHttpRequestInitializer
import com.wanna.framework.web.client.RestTemplate
import com.wanna.framework.web.http.HttpLogging
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
     * Logger
     */
    protected val logger = HttpLogging.getLogger(javaClass)

    /**
     * ClientHttpRequestFactory
     */
    private var requestFactory: ClientHttpRequestFactory = SimpleClientHttpRequestFactory()

    /**
     * 执行对于[ClientHttpRequest]的自定义的初始化器列表
     */
    private var clientHttpRequestInitializers = ArrayList<ClientHttpRequestInitializer>()

    open fun getRequestFactory(): ClientHttpRequestFactory = this.requestFactory

    open fun setRequestFactory(requestFactory: ClientHttpRequestFactory) {
        this.requestFactory = requestFactory
    }

    /**
     * 获取到需要对[ClientHttpRequest]进行初始化的[ClientHttpRequestInitializer]列表
     *
     * @return clientHttpRequestInitializers
     */
    open fun getClientHttpRequestInitializers(): MutableList<ClientHttpRequestInitializer> =
        this.clientHttpRequestInitializers

    /**
     * 设置要使用的[ClientHttpRequestInitializer]列表
     *
     * @param clientHttpRequestInitializers
     */
    open fun setClientHttpRequestInitializers(clientHttpRequestInitializers: List<ClientHttpRequestInitializer>) {
        if (this.clientHttpRequestInitializers != clientHttpRequestInitializers) {
            this.clientHttpRequestInitializers.clear()
            this.clientHttpRequestInitializers.addAll(clientHttpRequestInitializers)
            AnnotationAwareOrderComparator.sort(this.clientHttpRequestInitializers)
        }
    }

    /**
     * 创建[ClientHttpRequest]
     *
     * @param url URL
     * @param method HTTP请求方式
     * @return ClientHttpRequest
     */
    protected open fun createRequest(url: URI, method: RequestMethod): ClientHttpRequest {
        val request = getRequestFactory().createRequest(url, method)
        initialize(request)

        if (logger.isDebugEnabled) {
            logger.debug("HTTP ${method.name} $url")
        }
        return request
    }

    /**
     * 利用[ClientHttpRequestInitializer], 去对[ClientHttpRequest]去进行初始化
     *
     * @param request request
     */
    private fun initialize(request: ClientHttpRequest) {
        clientHttpRequestInitializers.forEach { it.initialize(request) }
    }
}