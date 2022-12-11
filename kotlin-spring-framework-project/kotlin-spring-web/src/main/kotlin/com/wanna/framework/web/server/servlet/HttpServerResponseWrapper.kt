package com.wanna.framework.web.server.servlet

import com.wanna.framework.web.http.Cookie
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpStatus
import com.wanna.framework.web.server.HttpServerResponse
import java.io.OutputStream
import javax.servlet.http.HttpServletResponse

/**
 * [HttpServerResponse] Wrapper
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class HttpServerResponseWrapper(private val response: HttpServletResponse) : HttpServerResponse {
    /**
     * Cookies
     */
    private val cookies = ArrayList<Cookie>()

    /**
     * HttpHeaders
     */
    private val httpHeaders = HttpHeaders()

    init {
        // transform header
        for (headerName in response.headerNames) {
            for (headerValue in response.getHeaders(headerName)) {
                this.httpHeaders.add(headerName, headerValue)
            }
        }
    }

    override fun addHeader(name: String, value: String?) {
        response.addHeader(name, value)
        if (value != null) {
            this.httpHeaders.set(name, value)
        } else {
            this.httpHeaders.remove(name)
        }
    }

    override fun setHeader(name: String, value: String?) {
        response.setHeader(name, value)
        if (value != null) {
            this.httpHeaders.set(name, value)
        } else {
            this.httpHeaders.remove(name)
        }
    }

    override fun addCookie(cookie: Cookie) {
        this.cookies.add(cookie)
        val servletCookie = javax.servlet.http.Cookie(cookie.name, cookie.value)
        servletCookie.secure = cookie.secure
        servletCookie.isHttpOnly = cookie.httpOnly
        servletCookie.version = cookie.version
        servletCookie.maxAge = cookie.maxAge
        servletCookie.comment = cookie.comment
        servletCookie.path = cookie.path
        servletCookie.domain = cookie.domain
        response.addCookie(servletCookie)
    }

    override fun removeHeader(name: String): String? {
        val headerValue = this.getHeader(name)
        this.setHeader(name, null)
        return headerValue
    }

    override fun getHeader(name: String): String? = httpHeaders.getFirst(name)

    override fun sendError(statusCode: Int) {
        this.sendError(statusCode, "")
    }

    /**
     * get Status
     */
    override fun getStatusCode(): Int = response.status

    /**
     * set Status
     */
    override fun setStatus(statusCode: Int) {
        response.status = statusCode
    }

    override fun setStatus(status: HttpStatus) {
        response.status = status.value
    }

    override fun sendError(statusCode: Int, msg: String) {
        response.sendError(statusCode, msg)
    }

    override fun getOutputStream(): OutputStream = response.outputStream

    override fun getContentType(): String = response.contentType

    override fun getHeaders(): HttpHeaders = this.httpHeaders

    override fun getCookies(): Array<Cookie> = this.cookies.toTypedArray()

    override fun getMessage(): String = ""

    override fun flush() {
        // 先把header转移到response当中, 再去进行flush
        this.httpHeaders.forEach {
            for (headerValue in it.value) {
                response.addHeader(it.key, headerValue)
            }
        }
        // cookie应该不用转移...

        response.outputStream.flush()
    }
}