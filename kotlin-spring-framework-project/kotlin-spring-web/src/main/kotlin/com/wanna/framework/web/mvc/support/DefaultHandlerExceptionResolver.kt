package com.wanna.framework.web.mvc.support

import com.wanna.framework.beans.TypeMismatchException
import com.wanna.framework.web.bind.ServerRequestBindingException
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.http.HttpStatus
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 默认的[HandlerExceptionResolver]实现, 负责用来处理SpringMVC处理请求过程当中抛出来的异常成为HTTP的错误状态码
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
open class DefaultHandlerExceptionResolver : HandlerExceptionResolver {

    /**
     * 去解析SpringMVC处理请求过程当中发生的异常
     *
     * @param request request
     * @param response response
     * @param handler handler
     * @param ex 要去处理的异常
     * @return 解析得到的ModelAndView
     */
    override fun resolveException(
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: Any?,
        ex: Throwable
    ): ModelAndView? {

        // 如果是类型不匹配异常
        if (ex is TypeMismatchException) {
            return handleTypeMismatch(ex, request, response, handler)
        }
        // 如果是绑定异常(@RequestParam/@RequestHeader的required=true, 但是实际上没有给定对应的参数值)
        if (ex is ServerRequestBindingException) {
            return handleServerRequestBindingException(ex, request, response, handler)
        }
        return null
    }

    /**
     * 处理[ServerRequestBindingException]异常(@RequestParam/@RequestHeader的required=true, 但是实际上没有给定对应的参数值)
     *
     * @param ex ServerRequest的绑定异常
     * @param request request
     * @param response response
     * @param handler handler
     * @return ModelAndView
     */
    protected open fun handleServerRequestBindingException(
        ex: ServerRequestBindingException,
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: Any?
    ): ModelAndView? {
        // sendError 400
        response.sendError(HttpStatus.BAD_REQUEST.value, ex.message ?: "")
        response.flush()

        // 返回空的ModelAndView代表不去渲染视图...
        return ModelAndView()
    }

    /**
     * 处理类型不匹配异常[TypeMismatchException]
     *
     * @param ex 类型不匹配异常
     * @param request request
     * @param response response
     * @param handler handler
     * @return ModelAndView
     */
    protected open fun handleTypeMismatch(
        ex: TypeMismatchException,
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: Any?
    ): ModelAndView? {

        // sendError 400
        response.sendError(HttpStatus.BAD_REQUEST.value)
        response.flush()

        // 返回空的ModelAndView代表不去渲染视图...
        return ModelAndView()
    }
}