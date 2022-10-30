package com.wanna.framework.web

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.handler.HandlerAdapter
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 处理请求的Handler
 */
interface DispatcherHandler {

    /**
     * 获取所有处理请求映射的[HandlerMapping]，负责将一个Http请求去找到对应的Handler去进行处理
     *
     * @return HandlerMappings
     */
    @Nullable
    fun getHandlerMappings(): List<HandlerMapping>?

    /**
     * 获取所有处理请求的[HandlerAdapter]
     *
     * @return HandlerAdapters
     */
    @Nullable
    fun getHandlerAdapters(): List<HandlerAdapter>?

    /**
     * 处理(派发)本次请求
     *
     * @param request request
     * @param response response
     */
    fun doDispatch(request: HttpServerRequest, response: HttpServerResponse)
}