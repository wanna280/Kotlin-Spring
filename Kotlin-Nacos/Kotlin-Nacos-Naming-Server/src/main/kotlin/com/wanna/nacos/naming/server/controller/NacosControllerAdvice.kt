package com.wanna.nacos.naming.server.controller

import com.wanna.framework.web.method.annotation.ControllerAdvice
import com.wanna.framework.web.method.annotation.ExceptionHandler
import com.wanna.framework.web.method.annotation.ResponseBody
import com.wanna.framework.web.method.support.InvocableHandlerMethod

@ControllerAdvice
open class NacosControllerAdvice {

    @ResponseBody
    @ExceptionHandler([IllegalStateException::class])
    open fun handleError(handlerMethod: InvocableHandlerMethod, ex: IllegalStateException): String {
        return "error"
    }
}