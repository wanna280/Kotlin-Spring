package com.wanna.framework.web.ui

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

class DefaultView : View {
    /**
     * 渲染视图
     *
     * @param model model
     * @param request request
     * @param response response
     */
    override fun render(model: Map<String, *>?, request: HttpServerRequest, response: HttpServerResponse) {
        response.flush() // flush
    }
}