package com.wanna.framework.web.http

import com.wanna.framework.lang.Nullable

/**
 * Http的响应Entity
 *
 * @param headers 响应的HttpHeaders
 * @param status 响应状态码
 * @param body 响应的ResponseBody
 */
open class ResponseEntity<T : Any>(val status: Any, headers: HttpHeaders, @Nullable body: T?) :
    HttpEntity<T>(headers, body) {

}