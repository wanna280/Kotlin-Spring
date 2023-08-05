package com.wanna.framework.web.http.client

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.StringUtils
import com.wanna.framework.web.http.HttpHeaders
import java.io.InputStream
import java.net.HttpURLConnection

/**
 * 基于JDK当中的基础设施(facilities), 去进行实现的[ClientHttpResponse],
 * 通过[SimpleBufferingClientHttpRequest.execute]和[SimpleStreamingClientHttpRequest.execute]方法去
 * 执行HTTP请求从而获取到
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @param connection HttpURLConnection
 *
 * @see HttpURLConnection
 */
internal class SimpleClientHttpResponse(private val connection: HttpURLConnection) : ClientHttpResponse {

    /**
     * 响应的HttpHeaders
     */
    @Nullable
    private var headers: HttpHeaders? = null

    /**
     * HTTP响应的输入流
     */
    @Nullable
    private var responseStream: InputStream? = null


    /**
     * 获取HTTP的响应的输入流
     *
     * @return 响应输入流
     */
    override fun getBody(): InputStream {
        this.responseStream = this.connection.errorStream ?: this.connection.inputStream
        return this.responseStream!!
    }

    /**
     * 从[HttpURLConnection]当中, 去获取到HTTP响应的[HttpHeaders]
     *
     * @return HttpHeaders
     */
    override fun getHeaders(): HttpHeaders {
        var headers: HttpHeaders? = this.headers
        if (headers == null) {
            headers = HttpHeaders()

            // 对于第一个Header, 可以直接取, 因为大多数的HttpURLConnection第一个Header都是StatusLine
            var name = this.connection.getHeaderFieldKey(0)
            if (StringUtils.hasText(name)) {
                headers.add(name, this.connection.getHeaderField(0))
            }

            // 根据HttpURLConnection后续的所有的Headers, 尝试去进行分别添加到HttpHeaders当中...
            var index = 1
            while (true) {
                name = this.connection.getHeaderFieldKey(index)
                // 通过探测name是否为空去进行检查...
                if (!StringUtils.hasText(name)) {
                    break
                }
                headers.add(name, this.connection.getHeaderField(index))
                index++
            }
            this.headers = headers
        }
        return headers
    }

    /**
     * 获取到响应状态码
     *
     * @return 响应状态码
     */
    override fun getStatusCode(): Int = connection.responseCode

    /**
     * 关闭Response, 需要将HTTP响应的输入流去进行关闭
     *
     * @see InputStream.close
     */
    override fun close() {
        try {
            if (this.responseStream == null) {
                getBody()
            }
            this.responseStream?.close()
        } catch (ex: Exception) {
            // ignore
        }
    }
}