package com.wanna.framework.web.http.client

/**
 * ClientHttpRequest的执行器链条，设计和Servlet的Filter类似，需要自己调用execute方法去进行放行
 */
@FunctionalInterface
interface ClientHttpRequestExecution {

    /**
     * 执行(放行)目标请求，获取Response，差不多等价于Filter的doFilter方法；
     *
     * Note: 如果不向下放行，完全可以自己去处理目标请求；如果放行，那么才会交给request去处理真正的请求
     *
     * @param request 原始的客户端请求
     * @param body RequestBody
     * @return 处理完成的Response
     */
    fun execute(request: ClientHttpRequest, body: ByteArray) : ClientHttpResponse
}