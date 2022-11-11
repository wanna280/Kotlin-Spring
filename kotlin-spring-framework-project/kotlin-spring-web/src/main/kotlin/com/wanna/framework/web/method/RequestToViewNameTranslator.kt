package com.wanna.framework.web.method

import com.wanna.framework.web.server.HttpServerRequest

/**
 * 将请求转换为viewName的翻译器
 */
interface RequestToViewNameTranslator {
    fun getViewName(request: HttpServerRequest): String?
}