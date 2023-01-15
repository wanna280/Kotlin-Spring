package com.wanna.boot.actuate.endpoint.web

/**
 * WebEndpoint下的HttpMethod(只支持GET/POST/DELETE, 对应的OperationType为READ/WRITE/DELETE, 一一对应)
 *
 * @see com.wanna.boot.actuate.endpoint.OperationType
 */
enum class WebEndpointHttpMethod {
    GET, POST, DELETE
}