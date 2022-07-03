package com.wanna.framework.web.server

import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpStatus
import java.io.OutputStream

/**
 * HttpServerResponse
 */
interface HttpServerResponse {
    companion object {
        const val SC_NOT_FOUND = 404
    }

    /**
     * 获取HttpServerResponse的ResponseBody输出流
     *
     * @return 当前的response的ResponseBody输出流
     */
    fun getOutputStream(): OutputStream

    /**
     * 获取当前的HttpServerResponse的HttpHeaders
     *
     * @return HttpHeaders of this response
     */
    fun getHeaders(): HttpHeaders

    /**
     * 根据headerName，去移除一个header
     *
     * @param name headerName
     * @return 之前的旧的headerValue(如果有多个，使用"; "去进行分割)
     */
    fun removeHeader(name: String): String?

    /**
     * 根据name和value去设置一个Header(如果之前已经有该header，那么直接清除掉之前所有的)
     *
     * @param name headerName
     * @param value headerValue(为null时表示移除)
     */
    fun setHeader(name: String, value: String?)

    /**
     * 根据name和value去添加一个Header(如果之前已经有该header，那么在原来的基础上去进行扩充)
     *
     * @param name headerName
     * @param value headerValue(为null时表示移除)
     */
    fun addHeader(name: String, value: String?)

    /**
     * 根据headerName去设置一个Header
     *
     * @param name headerName
     * @return headerValue(如果有多个，使用"; "去进行分割；如果不存在return null)
     */
    fun getHeader(name: String): String?

    /**
     * 获取response的响应类型
     *
     * @return 响应类型(比如"application/json")
     */
    fun getContentType(): String

    /**
     * 获取响应状态码
     *
     * @return 响应状态码(比如200/404/500)
     */
    fun getStatusCode(): Int

    /**
     * 获取响应的消息，配合状态码去进行使用
     *
     * @return 响应携带的消息
     */
    fun getMessage(): String

    /**
     * 设置HttpStatus
     *
     * @param statusCode statusInt
     */
    fun setStatus(statusCode: Int)

    /**
     * 设置HttpStatus
     *
     * @param status status
     */
    fun setStatus(status: HttpStatus)

    /**
     * sendError，msg采用默认的msg
     *
     * @param statusCode 状态码
     */
    fun sendError(statusCode: Int)

    /**
     * sendError，并同时设置Error的消息
     *
     * @param statusCode 状态码
     * @param msg message of error
     */
    fun sendError(statusCode: Int, msg: String)

    /**
     * flush Response，将Buffer当中的数据写出给客户端
     */
    fun flush()
}