package com.wanna.framework.web.http.client

import com.wanna.framework.util.StringUtils
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.HttpHeaders
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException

/**
 * 通过JDK的基础设施去实现待RequestBody的缓存[ClientHttpRequest], 通过[SimpleClientHttpRequestFactory]去进行创建
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @see AbstractBufferingClientHttpRequest
 *
 * @param connection HttpURLConnection
 * @param outputStreaming outputStreaming?
 *
 * @see HttpURLConnection
 */
internal class SimpleBufferingClientHttpRequest(
    private val connection: HttpURLConnection,
    private val outputStreaming: Boolean
) : AbstractBufferingClientHttpRequest() {

    /**
     * 获取到HTTP的请求方式
     *
     * @return requestMethod
     */
    override fun getMethod(): RequestMethod = RequestMethod.valueOf(connection.requestMethod)

    /**
     * 获取HTTP请求的URI
     *
     * @return request URI
     */
    override fun getURI(): URI {
        try {
            return connection.url.toURI()
        } catch (ex: URISyntaxException) {
            throw IllegalStateException("Could not get HttpURLConnection URI:${ex.message}", ex)
        }
    }

    /**
     * 根据[HttpURLConnection]发起HTTP请求, 并获取到[ClientHttpResponse]
     *
     * @param headers headers
     * @param bufferedOutput 缓存的字节流数组
     * @return ClientHttpResponse
     */
    override fun executeInternal(headers: HttpHeaders, bufferedOutput: ByteArray): ClientHttpResponse {
        // 先添加HttpHeaders
        addHeaders(connection, headers)

        // JDK1.8以下, 不支持DELETE请求去获取OutputStream
        if (getMethod() == RequestMethod.DELETE && bufferedOutput.isEmpty()) {
            connection.doOutput = false
        }

        // 如果"output streaming"为true, 那么需要将HttpURLConnection去设置为固定长度的流的模式...
        if (connection.doOutput && this.outputStreaming) {
            connection.setFixedLengthStreamingMode(bufferedOutput.size)
        }

        // doConnect
        this.connection.connect()

        // 如果可以获取到OutputStream的话, 将BufferedOutput缓冲的内容, 去执行写给Connection当中...
        if (connection.doOutput) {
            connection.outputStream.use { it.write(bufferedOutput) }
        } else {
            // try get ResponseCode
            connection.responseCode
        }

        // 返回ClientHttpResponse
        return SimpleClientHttpResponse(connection)
    }


    companion object {

        /**
         * 将[HttpHeaders]当中的全部请求头, 都去添加到[HttpURLConnection]当中去
         *
         * @param connection HttpURLConnection
         * @param headers HttpHeaders
         */
        @JvmStatic
        fun addHeaders(connection: HttpURLConnection, headers: HttpHeaders) {
            val requestMethod = connection.requestMethod

            // 如果为PUT/DELETE请求, 那么默认将Accept设置为"*/*", 因为PUT/DELETE一般没有RequestBody
            if (requestMethod.equals(RequestMethod.PUT.name, true)
                || requestMethod.equals(RequestMethod.DELETE.name, true)
            ) {
                if (!StringUtils.hasText(headers.getFirst(HttpHeaders.ACCEPT))) {
                    headers.set(HttpHeaders.ACCEPT, "*/*")
                }
            }

            // 将所有的Header全部添加到HttpURLConnection当中...
            for ((headerName, headerValues) in headers) {
                if (HttpHeaders.COOKIE.equals(headerName, true)) {
                    val value = StringUtils.collectionToCommaDelimitedString(headerValues)
                    connection.setRequestProperty(headerName, value)
                }
                for (headerValue in headerValues) {
                    connection.addRequestProperty(headerName, headerValue)
                }
            }
        }
    }
}