package com.wanna.framework.web.server

import com.wanna.framework.web.bind.RequestMethod
import java.io.InputStream

class HttpServerRequest {
    companion object {
        const val COMMA = "; "
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
    private var headers = LinkedHashMap<String, String>()

    // params
    private var params = LinkedHashMap<String, String>()

    private var inputStream: InputStream? = null

    fun setInputStream(inputStream: InputStream) {
        this.inputStream = inputStream
    }

    fun getInputStream(): InputStream {
        return this.inputStream!!
    }

    /**
     * 添加参数，如果value为空的话，移除该name的param
     */
    fun addParam(name: String, value: String?) {
        if (value != null) {
            this.params[name] = value
        } else {
            this.params -= name
        }
    }

    /**
     * 根据name获取param
     */
    fun getParam(name: String): String? {
        return this.params[name]
    }

    /**
     * 移除一个参数
     */
    fun removeParam(name: String) {
        this.params -= name
    }

    /**
     * 添加Header，如果value为空的话，标识移除该name的header
     */
    fun addHeader(name: String, value: String?) {
        if (value != null) {
            this.headers[name] = value
        } else {
            this.headers -= name
        }
    }

    /**
     * 设置某个Header的值
     */
    fun setHeader(name: String, value: String) {
        this.headers[name] = value
    }

    /**
     * 根据name去获取到headerValue
     */
    fun getHeader(name: String): String? {
        return this.headers[name]
    }

    fun getHeaders(): Map<String, String> {
        return this.headers
    }

    fun getUri() = this.uri
    fun setUri(uri: String) {
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

            // 如果有多个key相同的时候，应该将其使用";"分割
            var newParamVal = (this.params[key] ?: "") + COMMA + value
            if (newParamVal.startsWith(COMMA)) {
                newParamVal = newParamVal.substring(COMMA.length)
            }

            // 拼接param
            this.params[key] = newParamVal
        }
    }

    fun getUrl() = this.url

    fun getMethod() = this.method
    fun setMethod(method: RequestMethod) {
        this.method = method
    }

}