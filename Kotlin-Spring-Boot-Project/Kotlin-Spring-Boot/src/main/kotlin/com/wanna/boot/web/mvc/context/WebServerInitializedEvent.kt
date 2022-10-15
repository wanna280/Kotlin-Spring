package com.wanna.boot.web.mvc.context

import com.wanna.boot.web.server.WebServer
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.event.ApplicationEvent

/**
 * WebServer已经完成初始化的事件
 *
 * @param webServer webServer
 */
abstract class WebServerInitializedEvent(val webServer: WebServer) : ApplicationEvent(webServer) {
    abstract fun getApplicationContext() : ApplicationContext
}