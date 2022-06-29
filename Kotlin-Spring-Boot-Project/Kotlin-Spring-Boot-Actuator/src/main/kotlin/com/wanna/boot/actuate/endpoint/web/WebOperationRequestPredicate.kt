package com.wanna.boot.actuate.endpoint.web

open class WebOperationRequestPredicate {

    private var path: String = ""

    private var httpMethod: WebEndpointHttpMethod = WebEndpointHttpMethod.GET

    open fun getPath(): String = this.path

    open fun setPath(path: String) {
        this.path = path
    }

    open fun setHttpMethod(httpMethod: WebEndpointHttpMethod) {
        this.httpMethod = httpMethod
    }

    open fun getHttpMethod(): WebEndpointHttpMethod = this.httpMethod


}