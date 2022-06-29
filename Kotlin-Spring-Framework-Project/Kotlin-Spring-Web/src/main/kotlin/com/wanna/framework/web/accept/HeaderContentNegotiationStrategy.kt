package com.wanna.framework.web.accept

import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于请求头的内容协商策略，获取请求头当中的Accept字段当中传递的参数列表，并将其解析为MediaType
 *
 * @see ContentNegotiationManager
 * @see ContentNegotiationStrategy
 */
open class HeaderContentNegotiationStrategy : ContentNegotiationStrategy {
    override fun resolveMediaTypes(webRequest: NativeWebRequest): List<MediaType> {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        val accept = request.getHeader("Accept")
        return MediaType.parseMediaTypes(accept)
    }
}