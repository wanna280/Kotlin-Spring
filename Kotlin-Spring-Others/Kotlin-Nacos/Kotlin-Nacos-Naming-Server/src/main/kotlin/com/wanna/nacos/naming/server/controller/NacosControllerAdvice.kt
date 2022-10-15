package com.wanna.nacos.naming.server.controller

import com.wanna.framework.web.bind.annotation.ControllerAdvice
import com.wanna.framework.web.bind.annotation.ExceptionHandler
import com.wanna.framework.web.bind.annotation.ResponseBody
import com.wanna.framework.web.method.support.InvocableHandlerMethod

@ControllerAdvice
open class NacosControllerAdvice {

    @ResponseBody
    @ExceptionHandler([IllegalStateException::class, IllegalArgumentException::class, NullPointerException::class])
    open fun handleError(handlerMethod: InvocableHandlerMethod, ex: IllegalStateException): String {
        return "IllegalStateException-->${ex.message}"
    }
}