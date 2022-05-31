package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpHeaders
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.InputStreamEntity
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * 基于Apache的HttpComponents/HttpClient实现的客户端Request
 *
 * @param httpClient Apache的HttpClient
 * @param httpRequest Apache的HttpRequest
 */
class HttpComponentsClientHttpRequest(private val httpClient: HttpClient, private val httpRequest: HttpUriRequest) :
    ClientHttpRequest {

    private val headers = HttpHeaders()

    private var body: OutputStream? = null

    /**
     * 在发送给Server时，需要使用的HttpHeaders
     */
    override fun getHeaders(): HttpHeaders {
        return headers
    }

    fun setBody(body: OutputStream) {
        this.body = body
    }

    /**
     * 在发送给Server时，需要携带的RequestBodu
     */
    override fun getBody(): OutputStream {
        return this.body!!
    }

    /**
     * 执行目标请求，并得到ClientHttpResponse
     *
     * @return 执行完成得到的ClientHttpResponse
     */
    override fun execute(): ClientHttpResponse {
        val httpRequest = this.httpRequest
        val body = getBody()

        var byteArray = ByteArray(0)
        if (body is ByteArrayOutputStream) {
            byteArray = body.toByteArray()
        }

        // 添加header
        headers.keys.forEach { httpRequest.addHeader(it, headers[it]?.joinToString("; ")) }

        // 添加愿意接收的响应类型为"*/*"
        httpRequest.addHeader(HttpHeaders.ACCEPT,"*/*")

        // 添加HttpEntity(RequestBody)
        if (httpRequest is HttpEntityEnclosingRequest) {
            httpRequest.entity = ByteArrayEntity(byteArray)
        }

        // 发送请求
        val response = httpClient.execute(httpRequest)

        // 构建ClientHttpResponse
        val httpResponse = HttpComponentsClientHttpResponse()

        httpResponse.setBody(response.entity.content)
        response.allHeaders.forEach { httpResponse.getHeaders().add(it.name, it.value) }
        return httpResponse
    }
}