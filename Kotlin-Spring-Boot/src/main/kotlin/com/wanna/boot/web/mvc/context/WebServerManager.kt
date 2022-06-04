package com.wanna.boot.web.mvc.context

import com.wanna.boot.web.mvc.server.WebServerFactory
import com.wanna.boot.web.server.WebServer

/**
 * 这是一个WebServerManager，负责去启动和关闭WebServer
 */
open class WebServerManager(
    private val applicationContext: MvcWebServerApplicationContext,
    private val factory: WebServerFactory
) {
    private var webServer: WebServer = factory.getWebServer()

    fun start() {
        this.webServer.start()

        // 发布WebServer已经初始化完成事件...
        this.applicationContext.publishEvent(MvcWebServerInitializedEvent(this.webServer, this.applicationContext))
    }

    fun getWebServer(): WebServer {
        return this.webServer
    }

    fun stop() {
        this.webServer.stop()
    }
}