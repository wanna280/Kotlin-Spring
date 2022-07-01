package com.wanna.boot.actuate.endpoint

/**
 * 执行目标Endpoint的请求失败异常
 */
open class InvalidEndpointRequestException(message: String, val reason: String, cause: Throwable? = null) :
    RuntimeException(message, cause)