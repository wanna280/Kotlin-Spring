package com.wanna.boot.web.mvc.context

import com.wanna.boot.web.server.WebServer
import com.wanna.boot.web.server.WebServerInitializedEvent
import com.wanna.framework.context.ApplicationContext

/**
 * MvcWebServer已经完成初始化的事件
 *
 * @param webServer WebServer
 * @param applicationContext applicationContext
 */
open class MvcWebServerInitializedEvent(webServer: WebServer, private val applicationContext: ApplicationContext) :
    WebServerInitializedEvent(webServer) {

    /**
     * 获取正在初始化WebServer时使用到的ApplicationContext
     *
     * @return ApplicationContext
     */
    override fun getApplicationContext(): ApplicationContext = this.applicationContext
}