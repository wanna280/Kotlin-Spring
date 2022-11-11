package com.wanna.framework.web.handler

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.ui.View

/**
 * 视图解析器，负责根据viewName去解析视图
 *
 * @see View
 */
interface ViewResolver {

    /**
     * 将viewName转换成为View视图对象
     *
     * @param viewName viewName
     * @return 解析得到的View(没有解析到的话，return null)
     */
    @Nullable
    fun resolveViewName(viewName: String): View?
}