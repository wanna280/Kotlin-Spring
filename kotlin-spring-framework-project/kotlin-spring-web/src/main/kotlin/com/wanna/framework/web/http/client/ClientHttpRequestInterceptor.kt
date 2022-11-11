package com.wanna.framework.web.http.client

import com.wanna.framework.web.client.RestTemplate
/**
 * 这是一个客户端的Http请求的拦截器，可以被应用给RestTemplate去完成请求的拦截
 *
 * @see RestTemplate
 */
@FunctionalInterface
interface ClientHttpRequestInterceptor {
    /**
     * 拦截目标客户端请求，去进行自定义的处理
     *
     * @param request 原始的请求
     * @param body RequestBody
     * @param execution 拦截器链
     * @return 返回的Response数据
     */
    fun intercept(
        request: ClientHttpRequest, body: ByteArray, execution: ClientHttpRequestExecution
    ): ClientHttpResponse
}