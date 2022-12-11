package com.wanna.boot.web.servlet

import javax.servlet.ServletContext

/**
 * [ServletContext]的初始化器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
fun interface ServletContextInitializer {
    /**
     * 在ServletContext启动时, 需要执行的初始化
     *
     * @param servletContext ServletContext
     */
    fun onStartup(servletContext: ServletContext)
}