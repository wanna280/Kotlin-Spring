package com.wanna.framework.web.accept

import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.MediaType

/**
 * 这是一个内容协商策略，支持去解析客户端媒体类型，通常会使用ContentNegotiationManager去组合多个策略去进行解析
 *
 * @see ContentNegotiationManager
 */
@FunctionalInterface
interface ContentNegotiationStrategy {

    companion object {
        val MEDIA_TYPE_ALL_LIST = arrayListOf<MediaType>(MediaType.ALL)
    }

    /**
     * 从request当中去解析客户端支持的媒体类型列表
     *
     * @param webRequest NativeWebRequest(request and response)
     * @return 客户端支持的媒体类型列表
     */
    fun resolveMediaTypes(webRequest: NativeWebRequest): List<MediaType>
}