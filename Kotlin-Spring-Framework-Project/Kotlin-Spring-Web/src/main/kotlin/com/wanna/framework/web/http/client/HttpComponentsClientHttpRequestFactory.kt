package com.wanna.framework.web.http.client

import com.wanna.framework.web.bind.annotation.RequestMethod
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import java.net.URI

class HttpComponentsClientHttpRequestFactory : ClientHttpRequestFactory {
    override fun createRequest(url: URI, method: RequestMethod): ClientHttpRequest {
        val httpClient = HttpClients.createDefault()
        val httpRequest = when (method) {
            RequestMethod.GET -> HttpGet(url)
            RequestMethod.POST -> HttpPost(url)
            RequestMethod.DELETE -> HttpDelete(url)
            RequestMethod.HEAD -> HttpHead(url)
            else -> throw IllegalStateException("不支持这种请求方式!!!")
        }
        return HttpComponentsClientHttpRequest(httpClient, httpRequest, method)
    }
}