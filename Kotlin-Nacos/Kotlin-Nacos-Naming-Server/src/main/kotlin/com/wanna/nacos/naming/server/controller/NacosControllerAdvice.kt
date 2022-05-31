package com.wanna.nacos.naming.server.controller

import com.wanna.framework.web.method.annotation.ControllerAdvice
import com.wanna.framework.web.method.annotation.ExceptionHandler
import com.wanna.framework.web.method.annotation.ResponseBody

@ControllerAdvice
open class NacosControllerAdvice {

    @ResponseBody
    @ExceptionHandler([IllegalStateException::class])
    open fun handleError(): String {
        return "error"
    }
}