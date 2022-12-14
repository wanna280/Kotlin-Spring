package com.wanna.boot.web.servlet.context

import com.wanna.boot.web.server.WebServer
import com.wanna.boot.web.server.WebServerInitializedEvent
import com.wanna.framework.context.ApplicationContext

/**
 * Servlet的WebServer已经初始化完成的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 *
 * @param webServer WebServer
 * @param applicationContext ApplicationContext
 */
open class ServletWebServerInitializedEvent(webServer: WebServer, private val applicationContext: ApplicationContext) :
    WebServerInitializedEvent(webServer) {
    override fun getApplicationContext(): ApplicationContext = this.applicationContext
}