package com.wanna.framework.web.ui

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

interface View {

    /**
     * 渲染视图
     *
     * @param model model
     * @param request request
     * @param response response
     */
    fun render(model: Map<String, *>?, request: HttpServerRequest, response: HttpServerResponse)
}