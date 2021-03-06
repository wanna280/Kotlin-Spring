package com.wanna.framework.web.http

/**
 * HttpEntity
 *
 * @param T RequestBody/ResponseBody的类型
 */
open class HttpEntity<T>(val headers: HttpHeaders, val body: T?) {

}