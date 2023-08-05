package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpHeaders
import java.io.OutputStream

/**
 * 抽象的[ClientHttpRequest]的实现, 为[ClientHttpRequest]的最终实现去提供了一些模板方法的实现
 *
 * @see AbstractBufferingClientHttpRequest
 * @see ClientHttpRequest
 */
abstract class AbstractClientHttpRequest : ClientHttpRequest {
    /**
     * HTTP请求的Headers
     */
    private val headers = HttpHeaders()

    /**
     * 当前Request是否已经执行过了? 不允许被多次执行
     */
    private var executed = false

    /**
     * 获取[HttpHeaders]
     *
     * @return Http Headers
     */
    override fun getHeaders() = headers

    /**
     * 实现发送HTTP的方法, 并提供一个internal方法, 为子类传入HttpHeaders
     *
     * @return ClientHttpResponse(当前请求的执行结果)
     */
    override fun execute(): ClientHttpResponse {
        val response = executeInternal(headers)
        executed = true
        return response
    }

    /**
     * 获取到HTTP请求的请求体的输出流
     *
     * @return OutputStream of RequestBody
     */
    override fun getBody(): OutputStream {
        return getBodyInternal(headers)
    }

    /**
     * 为子类提供[HttpHeaders]的访问, 并去执行HTTP请求的发送
     *
     * @param headers headers
     * @return ClientHttpResponse
     */
    abstract fun executeInternal(headers: HttpHeaders): ClientHttpResponse

    /**
     * 为子类去获取RequestBody的输出流时, 提供请求的[HttpHeaders]的访问
     *
     * @param headers headers
     * @return RequestBody的输出流
     */
    abstract fun getBodyInternal(headers: HttpHeaders): OutputStream
}