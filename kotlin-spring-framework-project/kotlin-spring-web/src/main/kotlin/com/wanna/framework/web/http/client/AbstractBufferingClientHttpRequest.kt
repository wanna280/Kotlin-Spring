package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpHeaders
import java.io.ByteArrayOutputStream
import java.io.OutputStream


/**
 * 带有缓冲的[ClientHttpRequest], 将RequestBody先写入到缓冲流当中, 最终再写入到RequestBody当中
 *
 * @see AbstractClientHttpRequest
 * @see bufferedOutput
 */
abstract class AbstractBufferingClientHttpRequest : AbstractClientHttpRequest() {

    /**
     * HTTP请求的RequestBody的缓冲流
     */
    private val bufferedOutput = ByteArrayOutputStream(1024)

    /**
     * 执行HTTP请求的发送, 并获取到[ClientHttpResponse]
     *
     * @param headers 请求的HttpHeaders信息
     * @return 发送HTTP请求的发送, 获取到的HttpResponse
     */
    override fun executeInternal(headers: HttpHeaders): ClientHttpResponse {
        return executeInternal(headers, bufferedOutput.toByteArray())
    }

    /**
     * 获取到HTTP请求的缓冲流, 为子类提供请求的[HttpHeaders]的访问
     *
     * @param headers HttpHeaders
     * @return RequestBody的输出流
     */
    override fun getBodyInternal(headers: HttpHeaders): OutputStream = this.bufferedOutput

    /**
     * 为子类提供Headers和BufferedOutput的访问
     *
     * @param headers headers
     * @param bufferedOutput bufferedOutput(ByteArray)
     * @return 发送HTTP请求的发送, 获取到的HttpResponse
     */
    abstract fun executeInternal(headers: HttpHeaders, bufferedOutput: ByteArray): ClientHttpResponse
}