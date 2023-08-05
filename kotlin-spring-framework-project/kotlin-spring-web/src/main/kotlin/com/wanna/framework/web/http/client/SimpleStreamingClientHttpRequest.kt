package com.wanna.framework.web.http.client

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.HttpHeaders
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException

/**
 * 直接以流的方式去进行RequestBody的写, 不带缓冲...
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @param connection HttpURLConnection
 * @param chuckSize 块大小
 * @param outputStreaming outputStreaming?
 *
 * @see HttpURLConnection
 */
internal class SimpleStreamingClientHttpRequest(
    private val connection: HttpURLConnection,
    private val chuckSize: Int,
    private val outputStreaming: Boolean
) : AbstractClientHttpRequest() {

    /**
     * HTTP响应
     */
    @Nullable
    private var body: OutputStream? = null

    /**
     * 获取本次HTTP请求的URI
     *
     * @return HTTP请求URI
     */
    override fun getURI(): URI {
        try {
            return connection.url.toURI()
        } catch (ex: URISyntaxException) {
            throw IllegalStateException("Could not get HttpURLConnection URI:${ex.message}", ex)
        }
    }

    /**
     * 获取本次HTTP的请求方式
     *
     * @return HTTP请求方式
     */
    override fun getMethod(): RequestMethod = RequestMethod.valueOf(connection.requestMethod)

    /**
     * 获取HTTP请求的请求体的输出流
     *
     * @return HTTP请求的请求体的输出流
     */
    override fun getBodyInternal(headers: HttpHeaders): OutputStream {
        var body = this.body
        if (body == null) {
            // 如果"output streaming"为true, 需要去设置chuckSize/fixedLength模式
            if (outputStreaming) {
                val contentLength = headers.getContentLength()
                if (contentLength < 0) {
                    this.connection.setChunkedStreamingMode(chuckSize)
                } else {
                    this.connection.setFixedLengthStreamingMode(contentLength)
                }
            }

            // 添加HttpHeaders
            SimpleBufferingClientHttpRequest.addHeaders(connection, headers)

            // doConnect
            this.connection.connect()

            // 获取RequestBody的输出流
            body = this.connection.outputStream
            this.body = body
        }
        return body!!
    }


    /**
     * 执行HTTP的请求的发送, 并获取到响应信息
     *
     * @param headers HTTP发送请求时的HttpHeaders
     * @return 执行HTTP请求, 获取到ddHttpResponse
     */
    override fun executeInternal(headers: HttpHeaders): ClientHttpResponse {
        try {
            if (this.body != null) {
                this.body?.close()
            } else {
                // 添加HttpHeaders
                SimpleBufferingClientHttpRequest.addHeaders(connection, headers)

                // doConnect
                this.connection.connect()

                // 立刻触发请求
                this.connection.responseCode
            }
        } catch (ex: Exception) {
            // ignore
        }
        return SimpleClientHttpResponse(connection)
    }
}