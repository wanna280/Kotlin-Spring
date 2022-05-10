package com.wanna.framework.web

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 这是处理请求当中的拦截器，支持在处理本次请求之前/之后去进行请求的拦截，并在请求处理完成之后去进行相关的收尾工作
 */
interface HandlerInterceptor {

    /**
     * 在处理请求之前，应该回调的方法
     *
     * @param request request
     * @param response response
     * @param handler 处理本次请求的Handler
     * @return 如果return true，本次请求放行；return false，结束后续处理请求的逻辑
     */
    fun preHandle(request: HttpServerRequest, response: HttpServerResponse, handler: Any): Boolean

    /**
     * 在处理请求之后，应该回调的方法
     *
     * @param request request
     * @param response response
     * @param handler 处理本次请求的Handler
     */
    fun postHandle(request: HttpServerRequest, response: HttpServerResponse, handler: Any)

    /**
     * 在完成本次请求之后，应该回调的方法
     *
     * @param request request
     * @param response response
     * @param handler 处理本次请求的Handler
     * @throws Throwable 处理请求过程当中发生的异常，没有发生异常为null
     */
    fun afterCompletion(request: HttpServerRequest, response: HttpServerResponse, handler: Any, ex: Throwable?)
}