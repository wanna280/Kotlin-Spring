package com.wanna.nacos.api.http.param

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/19
 */
class Header(_headers: Map<String, String>) {
    constructor() : this(emptyMap())

    private val headers: MutableMap<String, String> = LinkedHashMap()

    init {
        this.headers.putAll(_headers)
    }

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