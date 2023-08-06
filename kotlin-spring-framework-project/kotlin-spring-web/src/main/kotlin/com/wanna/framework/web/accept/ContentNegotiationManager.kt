package com.wanna.framework.web.accept

import com.wanna.framework.web.accept.ContentNegotiationStrategy.Companion.MEDIA_TYPE_ALL_LIST
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.MediaType

/**
 * 它是HTTP协议的内容协商管理器, 组合了多个内容协商策略去进行判断支持的响应方式;
 * 它主要是负责去解析请求的客户端究竟想要接收什么类型的响应, 比如可以从请求头当中去进行解析,
 * 也可以从请求参数当中去进行解析
 *
 * @see ContentNegotiationStrategy
 */
open class ContentNegotiationManager(strategies: Collection<ContentNegotiationStrategy>) : ContentNegotiationStrategy {

    /**
     * 提供一个无参数构造器, 默认支持Header的内容协商策略
     */
    constructor() : this(arrayListOf(HeaderContentNegotiationStrategy()))

    /**
     * 协商策略, 例如基于请求参数的内容协商策略/基于请求头(header)的内容协商策略
     */
    private val strategies = ArrayList(strategies)

    /**
     * 根据request去解析出来客户端愿意接收的合适的MediaType
     *
     * @param webRequest WebRequest
     * @return 从request当中解析出来合适的MediaType
     */
    override fun resolveMediaTypes(webRequest: NativeWebRequest): List<MediaType> {
        this.strategies.forEach {
            val mediaTypes = it.resolveMediaTypes(webRequest)
            if (mediaTypes == MEDIA_TYPE_ALL_LIST) {
                return@forEach
            }
            return mediaTypes
        }
        return MEDIA_TYPE_ALL_LIST
    }
}