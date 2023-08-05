package com.wanna.framework.web.http

/**
 * HttpEntity, 封装[HttpHeaders]和RequestBody
 *
 * @param T RequestBody/ResponseBody的类型
 */
open class HttpEntity<T : Any>(val headers: HttpHeaders, val body: T?) {

    companion object {
        /**
         * 空的HttpEntity单例对象
         */
        @JvmField
        val EMPTY = HttpEntity(HttpHeaders(), null)
    }
}