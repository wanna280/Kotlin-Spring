package com.wanna.boot.actuate.endpoint.web

open class WebOperationRequestPredicate(
    private val path: String,
    private val httpMethod: WebEndpointHttpMethod,
) {
    open fun getPath(): String = this.path
    open fun getHttpMethod(): WebEndpointHttpMethod = this.httpMethod
}