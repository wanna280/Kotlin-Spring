package com.wanna.boot.web.reactive.context

import com.wanna.boot.web.context.WebServerInitializedEvent
import com.wanna.boot.web.server.WebServer
import com.wanna.framework.context.ApplicationContext

/**
 * ReactiveWebServer已经完成初始化的事件
 *
 * @param webServer WebServer
 * @param applicationContext applicationContext
 */
open class ReactiveWebServerInitializedEvent(webServer: WebServer, private val applicationContext: ApplicationContext) :
    WebServerInitializedEvent(webServer) {

    override fun getApplicationContext(): ApplicationContext {
        return this.applicationContext
    }
}