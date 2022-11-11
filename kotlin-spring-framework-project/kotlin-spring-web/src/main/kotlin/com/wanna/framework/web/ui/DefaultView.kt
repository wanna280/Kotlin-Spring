package com.wanna.framework.web.ui

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 默认的View的实现
 *
 * @see View
 */
class DefaultView : View {
    /**
     * 渲染视图
     *
     * @param model model数据
     * @param request request
     * @param response response
     */
    override fun render(@Nullable model: Map<String, *>?, request: HttpServerRequest, response: HttpServerResponse) {
        response.flush() // flush
    }
}