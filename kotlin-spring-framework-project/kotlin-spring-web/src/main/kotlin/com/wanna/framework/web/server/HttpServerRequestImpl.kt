package com.wanna.framework.web.server

import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.Cookie
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.server.HttpServerRequest.Companion.COMMA
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL

/**
 * HttpServerRequest的默认实现
 *
 * @author jianchao.jia
 * @version 1.0
 */
open class HttpServerRequestImpl : HttpServerRequest {
    /**
     * 请求方式
     */
    private var method = RequestMethod.GET

    /**
     * schema(protocol)
     */
    var scheme: String = "http"

    /**
     * 完成请求路径, 包括query部分
     */
    private var uri = "/"

    /**
     * 请求路径, 不包含query部分
     */
    private var url = "/"

    /**
     * remoteHost
     */
    private var remoteHost: String = ""

    /**
     * remoteIp
     */
    private var remoteIp: String = ""

    /**
     * remote Port
     */
    private var remotePort: Int = -1

    // headers
    private val headers = HttpHeaders()

    /**
     * Cookies
     */
    private var cookies: Array<Cookie>? = null

    /**
     * params
     */
    private val params = LinkedMultiValueMap<String, String>()

    /**
     * attributes
     */
    private val attributes = LinkedHashMap<String, Any?>()

    /**
     * ActionHook, 当给予对应的状态码时, 应该产生的动作
     */
    private var actionHook: ActionHook? = null

    /**
     * AsyncContext
     */
    private var asyncContext: AsyncContextImpl? = null

    /**
     * InputStream
     */
    private var inputStream: InputStream? = null

    open fun setInputStream(inputStream: InputStream) {
        this.inputStream = inputStream
    }

    /**
     * 获取request的输入流, 可以从输出流当中获取RequestBody
     *
     * @return RequestBody的输入流
     */
    override fun getInputStream(): InputStream {
        return this.inputStream ?: ByteArrayInputStream(ByteArray(0))
    }

    /**
     * 获取本次请求的Cookie列表
     *
     * @return Cookie列表
     */
    override fun getCookies() = cookies ?: emptyArray()

    /**
     * 设置Cookie
     *
     * @param cookies Cookies
     */
    open fun setCookies(vararg cookies: Cookie) {
        this.cookies = arrayOf(*cookies)
    }

    /**
     * 设置request的具体的参数(如果之前已经有该参数了, 那么直接去进行替换)
     *
     * @param name paramName
     * @param value paramValue(为null时代表移除)
     */
    override fun setParam(name: String, value: String?) {
        if (value != null) {
            this.params.set(name, value)
        } else {
            this.params -= name
        }
    }

    /**
     * 添加参数, 如果value为空的话, 移除该name的param
     *
     * @param name paramName
     * @param value paramValue(为null时代表移除)
     */
    override fun addParam(name: String, value: String?) {
        if (value != null) {
            this.params.add(name, value)
        } else {
            this.params -= name
        }
    }

    override fun getFirstParam(name: String): String? {
        return params.getFirst(name)
    }

    /**
     * 根据name获取param
     *
     * @param name paramName
     * @return 给定paramName获取到的参数列表(如果存在有多个param, 那么使用"; "去进行分割)
     */
    override fun getParam(name: String): String? {
        return this.params[name]?.joinToString(COMMA)
    }

    /**
     * 获取ParamMap
     *
     * @return paramMap
     */
    override fun getParamMap(): Map<String, List<String>> {
        return this.params
    }

    /**
     * 根据name移除一个参数
     *
     * @param name paramName
     */
    override fun removeParam(name: String) {
        this.params -= name
    }

    override fun getParamNames(): Set<String> {
        return HashSet(this.params.keys)
    }

    /**
     * 添加Header, 如果value为空的话, 标识移除该name的header
     *
     * @param name headerName
     * @param value headerValue
     */
    override fun addHeader(name: String, value: String?) {
        if (value != null) {
            this.headers.add(name, value)
        } else {
            this.headers -= name
        }
    }

    /**
     * 设置某个Header的值
     *
     * @param name headerName
     * @param value headerValue
     */
    override fun setHeader(name: String, value: String) {
        this.headers.set(name, value)
    }

    /**
     * 根据name去获取到headerValue
     *
     * @param name headerName
     * @return 根据name去获取到的headerValue(如果该header存在有多个值, 那么使用"; "去进行分割)
     */
    override fun getHeader(name: String): String? {
        return this.headers[name]?.joinToString(COMMA)
    }

    override fun getFirstHeader(name: String): String? {
        return this.headers.getFirst(name)
    }

    /**
     * 获取当前request的HttpHeaders
     *
     * @return HttpHeaders of request
     */
    override fun getHeaders(): HttpHeaders {
        return this.headers
    }

    override fun getHeaders(name: String): Collection<String> {
        val headers = LinkedHashSet<String>()
        this.headers[name]?.forEach(headers::add)
        return headers
    }

    override fun getHeaderNames(): Set<String> {
        return kotlin.collections.HashSet(this.headers.keys)
    }

    override fun getAttribute(name: String): Any? {
        return this.attributes[name]
    }

    override fun setAttribute(name: String, value: Any?) {
        this.attributes[name] = value
    }

    override fun removeAttribute(name: String) {
        this.attributes.remove(name)
    }

    override fun getAttributeNames(): Set<String> {
        return HashSet(this.attributes.keys)
    }

    open fun setUri(uri: String) {
        this.uri = uri
    }

    open fun setUrl(url: String) {
        this.url = url
    }

    open fun init(init: HttpServerRequestImpl.() -> Unit) = init.invoke(this)

    override fun getLocalHost() = headers.getHost() ?: ""

    /**
     * 设置remoteHost
     *
     * @param remoteHost remoteHost
     */
    open fun setRemoteHost(remoteHost: String) {
        this.remoteHost = remoteHost
    }

    /**
     * 设置remoteIP
     *
     * @param remoteIp remoteIp
     */
    open fun setRemoteIp(remoteIp: String) {
        this.remoteIp = remoteIp
    }

    /**
     * 设置remotePort
     *
     * @param remotePort remotePort
     */
    open fun setRemotePort(remotePort: Int) {
        this.remotePort = remotePort
    }

    /**
     * 获取remote Port
     *
     * @return remote Port
     */
    override fun getRemotePort(): Int = this.remotePort

    /**
     * 获取remote Host
     *
     * @return remote Host
     */
    override fun getRemoteHost() = this.remoteHost

    /**
     * 获取远程的IP
     *
     * @return remoteIp
     */
    override fun getRemoteIp(): String = this.remoteIp

    override fun getServerPort() = URL(getSchema() + "://" + getLocalHost()).port

    override fun getSchema() = this.scheme

    open fun setSchema(scheme: String) {
        this.scheme = scheme
    }

    override fun getServerName() = URL(getSchema() + "://" + getLocalHost()).host!!

    override fun getUri() = this.uri

    override fun getUrl() = this.url
    override fun getMethod() = this.method

    open fun setMethod(method: RequestMethod) {
        this.method = method
    }

    override fun action(code: ActionCode, param: Any?) {
        this.actionHook?.action(code, param)
    }

    override fun startAsync(): AsyncContext {
        return startAsyncInternal(this, null)
    }

    override fun startAsync(request: HttpServerRequest, response: HttpServerResponse): AsyncContext {
        return startAsyncInternal(request, response)
    }

    private fun startAsyncInternal(request: HttpServerRequest?, response: HttpServerResponse?): AsyncContext {
        var asyncContext = this.asyncContext
        if (asyncContext == null) {
            asyncContext = AsyncContextImpl()
            this.asyncContext = asyncContext
        }
        asyncContext.setStarted(request, response)
        return this.asyncContext!!
    }

    override fun getAsyncContext(): AsyncContext? = this.asyncContext

    /**
     * 设置ActionHook
     *
     * @param actionHook 你想要使用的ActionHook
     */
    open fun setActionHook(actionHook: ActionHook) {
        this.actionHook = actionHook
    }
}