package com.wanna.framework.web.http.client

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.HttpHeaders
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ByteArrayEntity
import java.net.URI

/**
 * 基于Apache的HttpComponents/HttpClient实现的客户端Request
 *
 * @param httpClient Apache的HttpClient
 * @param httpRequest Apache的HttpRequest
 */
internal class HttpComponentsClientHttpRequest(
    private val httpClient: HttpClient,
    private val httpRequest: HttpUriRequest,
    private val method: RequestMethod = RequestMethod.GET
) : AbstractBufferingClientHttpRequest() {
    override fun getMethod() = method
    override fun getURI(): URI = this.httpRequest.uri

    override fun executeInternal(headers: HttpHeaders, bufferedOutput: ByteArray): ClientHttpResponse {
        val httpRequest = this.httpRequest
        // 添加header到ApacheHttpClient的HttpRequest当中
        headers.keys.forEach { httpRequest.addHeader(it, headers[it]?.joinToString("; ")) }
        // 添加愿意接收的响应类型为"*/*"
        httpRequest.addHeader(HttpHeaders.ACCEPT, "*/*")

        // 如果必要的话添加HttpEntity(RequestBody)
        if (httpRequest is HttpEntityEnclosingRequest) {
            httpRequest.entity = ByteArrayEntity(bufferedOutput)
        }
        // 使用Apache的HttpClient去发送真正的HTTP请求
        val response = httpClient.execute(httpRequest)
        // 构建ClientHttpResponse
        return HttpComponentsClientHttpResponse(response)
    }
}