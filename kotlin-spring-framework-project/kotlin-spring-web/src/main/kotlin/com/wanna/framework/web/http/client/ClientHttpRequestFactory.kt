package com.wanna.framework.web.http.client

import com.wanna.framework.web.bind.annotation.RequestMethod
import java.io.IOException
import java.net.URI

/**
 * [ClientHttpRequest]的工厂, 负责通过[createRequest]工厂方法方法去完成[ClientHttpRequest]
 *
 * @see ClientHttpRequest
 */
fun interface ClientHttpRequestFactory {

    /**
     * 根据给定的[URI]和[RequestMethod], 去创建一个新的[ClientHttpRequest]实例对象
     *
     * @param uri uri
     * @param method HTTP请求方法
     * @return 创建出来的新的ClientHttpRequest对象
     */
    @Throws(IOException::class)
    fun createRequest(uri: URI, method: RequestMethod): ClientHttpRequest
}