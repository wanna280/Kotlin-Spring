package com.wanna.framework.web.accept

import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于参数的内容协商策略，支持从请求参数当中获取指定参数名(默认为format)去获取媒体类型，从而去获取客户端想要接收的媒体类型
 *
 * @see ContentNegotiationStrategy
 */
open class ParameterContentNegotiationStrategy : ContentNegotiationStrategy {

    // 参数名，可以去进行自定义
    var parameterName = "format"

    override fun resolveMediaTypes(webRequest: NativeWebRequest): List<MediaType> {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        val formatParam = request.getParam(parameterName)
        return MediaType.parseMediaTypes(formatParam)
    }
}