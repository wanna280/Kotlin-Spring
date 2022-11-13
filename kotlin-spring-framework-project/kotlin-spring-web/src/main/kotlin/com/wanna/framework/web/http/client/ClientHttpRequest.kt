package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.http.HttpRequest

/**
 * 对一个客户端Http请求的描述，它可以获取去获取要进行发送的RequestBody(OutputStream)
 */
interface ClientHttpRequest : HttpOutputMessage, HttpRequest {

    /**
     * 执行目标HTTP请求，获取到ClientHttpResponse
     *
     * @return 执行目标请求，最终返回的ClientHttpResponse
     */
    fun execute(): ClientHttpResponse
}