package com.wanna.framework.web.http

class ResponseEntity<T>(val status: Any, headers: HttpHeaders, body: T?) : HttpEntity<T>(headers, body) {

}