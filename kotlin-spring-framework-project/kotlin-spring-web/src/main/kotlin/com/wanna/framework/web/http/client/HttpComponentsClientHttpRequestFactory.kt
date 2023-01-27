package com.wanna.framework.web.http.client

import com.wanna.framework.web.bind.annotation.RequestMethod
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import java.net.URI

/**
 * 基于Apache/HttpComponent去作为HttpClient, 从而实现[ClientHttpRequestFactory]
 *
 * @see ClientHttpRequestFactory
 * @see HttpComponentsClientHttpRequest
 */
open class HttpComponentsClientHttpRequestFactory : ClientHttpRequestFactory {

    /**
     * 为给定的URI和请求方式, 去创建出来一个[ClientHttpRequest]
     *
     * @param uri URI
     * @param method 请求方式
     * @return ClientHttpRequest
     */
    override fun createRequest(uri: URI, method: RequestMethod): ClientHttpRequest {
        val httpClient = HttpClients.createDefault()
        val httpRequest = when (method) {
            RequestMethod.GET -> HttpGet(uri)
            RequestMethod.POST -> HttpPost(uri)
            RequestMethod.DELETE -> HttpDelete(uri)
            RequestMethod.HEAD -> HttpHead(uri)
            else -> throw IllegalStateException("不支持这种请求方式!!!")
        }
        return HttpComponentsClientHttpRequest(httpClient, httpRequest, method)
    }
}