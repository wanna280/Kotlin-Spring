package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpInputMessage
import java.io.Closeable

/**
 * 客户端的Http响应
 */
interface ClientHttpResponse : HttpInputMessage, Closeable {

    /**
     * 获取HTTP响应状态码
     *
     * @return statusCode
     */
    fun getStatusCode(): Int

    /**
     * 关闭Response
     */
    override fun close()
}