package com.wanna.framework.web.mvc.annotation

import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 处理`@ResponseStatus`注解的[HandlerExceptionResolver]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
open class ResponseStatusExceptionResolver : HandlerExceptionResolver {

    override fun resolveException(
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: Any?,
        ex: Throwable
    ): ModelAndView? {
        // TODO
        return null
    }
}