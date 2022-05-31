package com.wanna.framework.web.http.client

/**
 * 这是一个客户端的Http请求的拦截器
 */
@FunctionalInterface
interface ClientHttpRequestInterceptor {
    /**
     * 拦截目标客户端请求，去进行自定义的处理
     */
    fun intercept(request: ClientHttpRequest, body: ByteArray)
}