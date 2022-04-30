package com.wanna.boot.web.reactive.context

import com.wanna.boot.web.reactive.server.ReactiveWebServerFactory
import com.wanna.boot.web.server.WebServer

/**
 * 这是一个WebServerManager，负责去启动和关闭WebServer
 */
class WebServerManager(
    private val applicationContext: ReactiveWebServerApplicationContext,
    private val factory: ReactiveWebServerFactory
) {
    private var webServer: WebServer = factory.getWebServer()

    fun start() {
        this.webServer.start()
    }

    fun getWebServer(): WebServer {
        return this.webServer
    }

    fun stop() {
        this.webServer.stop()
    }
}