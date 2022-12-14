package com.wanna.framework.web.server.servlet

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.Cookie
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerRequestImpl
import java.io.InputStream
import javax.servlet.http.HttpServletRequest

/**
 * [HttpServerRequest] Wrapper
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class HttpServerRequestWrapper(private val request: HttpServletRequest) : HttpServerRequestImpl(),
    HttpServerRequest {

    override fun getInputStream(): InputStream {
        return super.getInputStream()
    }

    override fun getUrl(): String {
        return super.getUrl()
    }

    override fun getUri(): String {
        return super.getUri()
    }

    override fun getMethod(): RequestMethod {
        return super.getMethod()
    }

    override fun getAttributeNames(): Set<String> = request.attributeNames.toList().toSet()

    override fun getLocalHost(): String = request.localAddr

    override fun getRemotePort(): Int = request.remotePort
    override fun getRemoteHost(): String = request.remoteHost

    override fun getAttribute(name: String): Any? = request.getAttribute(name)
    override fun setAttribute(name: String, value: Any?) = request.setAttribute(name, value)
    override fun removeAttribute(name: String) = request.removeAttribute(name)
}