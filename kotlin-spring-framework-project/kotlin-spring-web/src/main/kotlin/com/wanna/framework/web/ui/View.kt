package com.wanna.framework.web.ui

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 这是一个View的抽象, 它主要负责完成视图的渲染功能
 *
 * @see render
 */
interface View {

    /**
     * 渲染视图的具体逻辑, 需要子类自己去进行实现
     *
     * @param model model数据
     * @param request request
     * @param response response
     */
    fun render(@Nullable model: Map<String, *>?, request: HttpServerRequest, response: HttpServerResponse)
}