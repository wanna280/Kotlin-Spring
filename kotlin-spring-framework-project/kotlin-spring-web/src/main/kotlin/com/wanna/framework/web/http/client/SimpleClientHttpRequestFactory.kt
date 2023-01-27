package com.wanna.framework.web.http.client

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.bind.annotation.RequestMethod
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URI
import java.net.URL

/**
 * 基于JDK当中的基础设施(facilities), 去进行简单实现的[ClientHttpRequestFactory]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @see HttpURLConnection
 */
open class SimpleClientHttpRequestFactory : ClientHttpRequestFactory {

    companion object {

        /**
         * 默认分块的大小
         */
        private const val DEFAULT_CHUNK_SIZE = 4096
    }


    /**
     * HTTP请求的代理
     */
    @Nullable
    var proxy: Proxy? = null

    /**
     * 连接超时时间
     */
    var connectTimeout = -1

    /**
     * 读超时时间
     */
    var readTimeout = -1

    /**
     * 是否需要将RequestBody去进行缓存下来?
     */
    var bufferRequestBody = true

    /**
     * 如果以流的方式去进行写入时, 需要使用的分块大小
     *
     * @see HttpURLConnection.setChunkedStreamingMode
     */
    var chuckSize = DEFAULT_CHUNK_SIZE

    /**
     * 是否需要以流的方式去进行RequestBody的写入? 如果是的话, 那么认证(authentication)/重定向(redirection)的请求将不能被自动处理
     *
     * 如果[outputStreaming]为false的话, 那么[HttpURLConnection.setFixedLengthStreamingMode]和[HttpURLConnection.setChunkedStreamingMode]
     * 这两个方法都不会被启用.
     *
     * @see HttpURLConnection.setFixedLengthStreamingMode
     */
    var outputStreaming = true

    /**
     * 创建[ClientHttpRequest]
     *
     * @param uri URI
     * @param method HTTP请求方式
     * @return ClientHttpRequest
     */
    override fun createRequest(uri: URI, method: RequestMethod): ClientHttpRequest {
        // 打开HttpURLConnection
        val urlConnection = openConnection(uri.toURL(), proxy)

        // 准备HttpURLConnection
        prepareConnection(urlConnection, method.name)

        // 根据是否需要缓存下来RequestBody, 去创建不同类型的ClientHttpRequest实例...
        if (bufferRequestBody) {
            return SimpleBufferingClientHttpRequest(urlConnection, outputStreaming)
        } else {
            return SimpleStreamingClientHttpRequest(urlConnection, chuckSize, outputStreaming)
        }
    }

    /**
     * 根据给定的URL, 根据该URL去打开并返回一个[HttpURLConnection]
     *
     * @param url URL
     * @param proxy HTTP代理
     * @throws IOException 如果打开[HttpURLConnection]失败
     * @throws IllegalStateException 如果打开的连接不是[HttpURLConnection]
     */
    @Throws(IOException::class)
    protected open fun openConnection(url: URL, @Nullable proxy: Proxy?): HttpURLConnection {
        val connection = if (proxy != null) url.openConnection(proxy) else url.openConnection()
        if (connection !is HttpURLConnection) {
            throw IllegalStateException("HttpURLConnection required for [$url], but got: $connection")
        }
        return connection
    }

    /**
     * 准备[HttpURLConnection], 提供模板方法, 子类可以根据模板方法去进行自定义
     *
     * @param connection HttpURLConnection
     * @param httpMethod HTTP请求方式(GET/POST/DELETE/PUT/PATCH等)
     */
    @Throws(IOException::class)
    protected open fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
        // 初始化相关超时时间参数
        if (this.connectTimeout > 0) {
            connection.connectTimeout = connectTimeout
        }
        if (this.readTimeout > 0) {
            connection.readTimeout = readTimeout
        }

        // 设置请求方式
        connection.requestMethod = httpMethod

        // 对于POST/PUT/PATCH/DELETE请求, 可能发生写(可能获取OutputStream)
        val mayWrite = httpMethod.equals(RequestMethod.POST.name, true)
                || httpMethod.equals(RequestMethod.PUT.name, true)
                || httpMethod.equals(RequestMethod.PATCH.name, true)
                || httpMethod.equals(RequestMethod.DELETE.name, true)

        // 只有某些请求方式, 才可能发生写(可能获取OutputStream)
        connection.doOutput = mayWrite

        // 可能发生读(可能获取InputStream)
        connection.doInput = true

    }
}