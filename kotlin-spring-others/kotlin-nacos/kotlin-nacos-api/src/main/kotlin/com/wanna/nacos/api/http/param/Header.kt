package com.wanna.nacos.api.http.param

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/19
 */
class Header {

    private val headers = LinkedHashMap<String, String>()

    fun addParam(key: String, value: String) {
        headers[key] = value
    }

    fun getHeaders(): Map<String, String> = headers

    fun getValue(key: String): String? {
        return headers[key]
    }

    fun addAll(headers: Map<String, String>) {
        this.headers.putAll(headers)
    }
}