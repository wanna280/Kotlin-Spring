package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpHeaders

/**
 * 抽象的ClientHttpRequest的实现
 */
abstract class AbstractClientHttpRequest : ClientHttpRequest {
    // Headers
    private val headers = HttpHeaders()

    // 是否已经执行过了？
    private var executed = false
    override fun getHeaders() = headers

    /**
     * 实现方法, 并提供一个internal方法, 为子类传入HttpHeaders
     *
     * @return ClientHttpResponse(当前请求的执行结果)
     */
    override fun execute(): ClientHttpResponse {
        val response = executeInternal(headers)
        executed = true
        return response
    }

    /**
     * 为子类提供HttpHeaders的访问
     *
     * @param headers headers
     */
    abstract fun executeInternal(headers: HttpHeaders): ClientHttpResponse
}