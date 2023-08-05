package com.wanna.framework.web.accept

import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于请求头的内容协商策略, 获取请求头当中的Accept字段当中传递的参数列表, 并将其解析为MediaType
 *
 * @see ContentNegotiationManager
 * @see ContentNegotiationStrategy
 */
open class HeaderContentNegotiationStrategy : ContentNegotiationStrategy {

    /**
     * 解析客户端想要去接收的数据的MediaType列表
     *
     * @param webRequest NativeWebRequest(request and response)
     * @return 从请求头的"Accept"当中去解析出来客户端想要接收的媒体类型
     */
    override fun resolveMediaTypes(webRequest: NativeWebRequest): List<MediaType> {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        val accept = request.getHeader(HttpHeaders.ACCEPT)
        return MediaType.parseMediaTypes(accept)
    }
}