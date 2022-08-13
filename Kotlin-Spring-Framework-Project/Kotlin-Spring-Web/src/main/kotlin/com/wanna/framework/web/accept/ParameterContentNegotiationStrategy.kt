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

    /**
     * 解析客户端想要接收的媒体类型，这里主要提供从请求参数的"format"当中去获取到，并完成解析
     *
     * @param webRequest NativeWebRequest(request and response)
     * @return 解析完成的MediaType列表
     */
    override fun resolveMediaTypes(webRequest: NativeWebRequest): List<MediaType> {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        val formatParam = request.getParam(parameterName)
        return MediaType.parseMediaTypes(formatParam)
    }
}