package com.wanna.framework.web.context

import com.wanna.framework.beans.factory.Aware
import javax.servlet.ServletContext

/**
 * 用于去进行自动注入ServletContext的Aware接口
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/14
 *
 * @see ServletContext
 */
fun interface ServletContextAware : Aware {

    /**
     * 注入ServletContext
     *
     * @param servletContext ServletContext
     */
    fun setServletContext(servletContext: ServletContext)
}