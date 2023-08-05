package com.wanna.framework.web.cors

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * Cors的处理器, 提供去处理一个跨域请求, 判断一个跨域请求是否合法
 */
interface CorsProcessor {

    /**
     * 处理一个跨域请求
     *
     * @param request request
     * @param response response
     * @param config CrosConfiguration
     */
    fun processRequest(request: HttpServerRequest, response: HttpServerResponse, config: CorsConfiguration?): Boolean
}