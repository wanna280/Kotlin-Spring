package com.wanna.boot.actuate.endpoint

/**
 * 执行目标Endpoint的请求失败异常
 *
 * @param message message
 * @param reason 失败的原因
 * @param cause 原因(ex, 可以为null)
 */
open class InvalidEndpointRequestException(message: String, val reason: String, cause: Throwable? = null) :
    RuntimeException(message, cause)