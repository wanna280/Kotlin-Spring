package com.wanna.framework.web.server

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.http.Cookie
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpStatus
import com.wanna.framework.web.http.MediaType

/**
 * HttpServerResponse的默认实现
 *
 * @author jianchao.jia
 * @version 1.0
 */
open class HttpServerResponseImpl : HttpServerResponse {
    companion object {
        private const val COMMA = "; "

        /**
         * 默认的响应类型
         */
        private const val DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON_UTF8_VALUE
    }

    // flush Callback
    private var flushCallback: ((HttpServerResponseImpl) -> Unit)? = null

    // 响应状态码，默认为200
    private var statusCode: Int = HttpStatus.SUCCESS.value

    // message
    private var message: String = HttpStatus.SUCCESS.reasonPhase

    // ResponseBody的输出流
    private val outputStream = ResponseOutputStream(this, 1024)

    // headers
    private val headers = HttpHeaders()

    /**
     * Cookies
     */
    private val cookies = ArrayList<Cookie>()

    /**
     * 获取当前的HttpServerResponse的HttpHeaders
     *
     * @return HttpHeaders of this response
     */
    override fun getHeaders() = this.headers

    /**
     * 往Response当中添加一个Cookie
     *
     * @param cookie Cookie
     */
    override fun addCookie(cookie: Cookie) {
        this.cookies.add(cookie)
    }

    /**
     * 获取Cookies
     *
     * @return Cookies
     */
    override fun getCookies() = this.cookies.toTypedArray()

    /**
     * 根据headerName，去移除一个header
     *
     * @param name headerName
     * @return 之前的旧的headerValue(如果有多个，使用"; "去进行分割)
     */
    @Nullable
    override fun removeHeader(name: String) = this.headers.remove(name)?.joinToString(COMMA)

    /**
     * 根据name和value去设置一个Header(如果之前已经有该header，那么直接清除掉之前所有的)
     *
     * @param name headerName
     * @param value headerValue(为null时表示移除)
     */
    override fun setHeader(name: String, value: String?) {
        if (value != null) {
            this.headers.set(name, value)
        } else {
            this.headers -= name
        }
    }

    /**
     * 根据name和value去添加一个Header(如果之前已经有该header，那么在原来的基础上去进行扩充)
     *
     * @param name headerName
     * @param value headerValue(为null时表示移除)
     */
    override fun addHeader(name: String, value: String?) {
        if (value != null) {
            this.headers.add(name, value)
        } else {
            this.headers.remove(name)
        }
    }

    /**
     * 根据headerName去设置一个Header
     *
     * @param name headerName
     * @return headerValue(如果有多个，使用"; "去进行分割；如果不存在return null)
     */
    override fun getHeader(name: String) = headers[name]?.joinToString(COMMA)

    /**
     * 获取response的响应类型
     *
     * @return 响应类型(比如"application/json")
     */
    override fun getContentType() = headers.getFirst(HttpHeaders.CONTENT_TYPE) ?: DEFAULT_CONTENT_TYPE

    /**
     * 获取响应状态码
     *
     * @return 响应状态码(比如404，500)
     */
    override fun getStatusCode() = this.statusCode

    /**
     * 获取响应的消息，配合状态码去进行使用
     *
     * @return 响应携带的消息
     */
    override fun getMessage() = this.message

    /**
     * 获取HttpServerResponse的ResponseBody输出流
     *
     * @return 当前的response的ResponseBody输出流
     */
    override fun getOutputStream() = this.outputStream

    /**
     * sendError，msg采用默认的msg
     *
     * @param statusCode 状态码
     */
    override fun sendError(statusCode: Int) = sendError(statusCode, "")

    /**
     * 设置响应状态码
     *
     * @param statusCode 响应状态码
     */
    override fun setStatus(statusCode: Int) {
        this.statusCode = statusCode
    }

    /**
     * 设置响应状态吗
     *
     * @param status 响应状态码
     */
    override fun setStatus(status: HttpStatus): Unit = setStatus(status.value)

    /**
     * sendError，并同时设置Error的消息
     *
     * @param statusCode 状态码
     * @param msg message of error
     */
    override fun sendError(statusCode: Int, msg: String) {
        this.statusCode = statusCode
        this.message = msg
    }

    /**
     * 设置flush的回调callback
     *
     * @param callback 你先要去进行刷新的操作的callback
     */
    open fun initFlushCallback(callback: HttpServerResponseImpl.() -> Unit) {
        this.flushCallback = callback
    }

    /**
     * flush
     */
    override fun flush() {
        this.flushCallback?.invoke(this)  // invoke callback
        this.outputStream.reset()  // reset
    }
}