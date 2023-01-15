package com.wanna.framework.web.handler

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * Handler处理过程当中的异常的解析器, 负责去处理本次请求当中的异常
 */
fun interface HandlerExceptionResolver {

    /**
     * 真正地去解析处理请求过程当中发生的异常
     *
     * @param request request
     * @param response response
     * @param handler handler
     * @param ex ex
     * @return 解析异常得到的ModelAndView(return null代表处理失败; return 空的ModelAndView代表已经处理完成了, 并且无需渲染视图)
     */
    @Nullable
    fun resolveException(
        request: HttpServerRequest, response: HttpServerResponse, @Nullable handler: Any?, ex: Throwable
    ): ModelAndView?
}