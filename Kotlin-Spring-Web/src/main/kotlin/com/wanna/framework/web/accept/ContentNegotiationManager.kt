package com.wanna.framework.web.accept

import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.MediaType

/**
 * 它是内容协商管理器，组合了多个内容协商策略去进行判断支持的响应方式；它主要是负责去解析请求的客户端究竟想要接收什么类型的响应
 *
 * @see ContentNegotiationStrategy
 */
open class ContentNegotiationManager(strategies: Collection<ContentNegotiationStrategy>) : ContentNegotiationStrategy {

    // 提供一个无参数构造器，默认支持Header的内容协商策略
    constructor() : this(arrayListOf(HeaderContentNegotiationStrategy()))

    // 协商策略，例如基于请求参数的内容协商策略/基于请求头(header)的内容协商策略
    private val strategies = ArrayList(strategies)

    override fun resolveMediaTypes(webRequest: NativeWebRequest): List<MediaType> {
        this.strategies.forEach {
            val mediaTypes = it.resolveMediaTypes(webRequest)
            if (mediaTypes == ContentNegotiationStrategy.MEDIA_TYPE_ALL_LIST) {
                return@forEach
            }
            return mediaTypes
        }
        return ContentNegotiationStrategy.MEDIA_TYPE_ALL_LIST
    }
}