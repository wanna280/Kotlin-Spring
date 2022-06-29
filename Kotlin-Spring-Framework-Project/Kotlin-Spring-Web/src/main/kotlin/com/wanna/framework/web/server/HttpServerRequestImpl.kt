package com.wanna.framework.web.server

import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.server.HttpServerRequest.Companion.COMMA
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap

open class HttpServerRequestImpl : HttpServerRequest {
    companion object {
        const val PARAM_SEPARATOR = "&"
        const val EQUAL = "="
    }

    // 请求方式
    private var method = RequestMethod.GET

    // 完成请求路径，包括query部分
    private var uri = "/"

    // 请求路径，不包含query部分
    private var url = "/"

    // headers
    private val headers = HttpHeaders()

    // params
    private val params = LinkedMultiValueMap<String, String>()

    // attributes
    private val attributes = LinkedHashMap<String, Any?>()

    private var inputStream: InputStream? = null

    open fun setInputStream(inputStream: InputStream) {
        this.inputStream = inputStream
    }

    /**
     * 获取request的输入流，可以从输出流当中获取RequestBody
     *
     * @return RequestBody的输入流
     */
    override fun getInputStream(): InputStream {
        return this.inputStream ?: ByteArrayInputStream(ByteArray(0))
    }

    /**
     * 设置request的具体的参数(如果之前已经有该参数了，那么直接去进行替换)
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
     * 添加参数，如果value为空的话，移除该name的param
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
     * @return 给定paramName获取到的参数列表(如果存在有多个param，那么使用"; "去进行分割)
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
     * 添加Header，如果value为空的话，标识移除该name的header
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
     * @return 根据name去获取到的headerValue(如果该header存在有多个值，那么使用"; "去进行分割)
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
        parseUriUrlAndParams(uri)
    }

    private fun parseUriUrlAndParams(uri: String) {
        this.uri = uri
        val indexOf = uri.indexOf("?")
        if (indexOf == -1) {
            this.url = uri
            return
        }
        this.url = uri.substring(0, indexOf) // url

        val params = uri.substring(indexOf + 1).split(PARAM_SEPARATOR)
        params.forEach {
            val eqIndex = it.indexOf(EQUAL)
            val key = it.substring(0, eqIndex)
            val value = it.substring(eqIndex + 1)
            // 拼接param
            this.params.add(key, value)
        }
    }


    override fun getUri() = this.uri
    override fun getUrl() = this.url
    override fun getMethod() = this.method

    open fun setMethod(method: RequestMethod) {
        this.method = method
    }
}