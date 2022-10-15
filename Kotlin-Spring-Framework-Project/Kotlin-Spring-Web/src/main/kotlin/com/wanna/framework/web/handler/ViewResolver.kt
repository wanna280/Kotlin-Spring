package com.wanna.framework.web.handler

import com.wanna.framework.web.ui.View

/**
 * 视图解析器，负责根据viewName去解析视图
 */
interface ViewResolver {
    fun resolveView(viewName: String) : View?
}