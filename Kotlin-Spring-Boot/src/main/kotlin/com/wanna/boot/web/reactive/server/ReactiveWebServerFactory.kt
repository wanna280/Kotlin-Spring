package com.wanna.boot.web.reactive.server

import com.wanna.boot.web.server.WebServer

/**
 * 这是一个Reactive的WebServerFactory
 */
interface ReactiveWebServerFactory {
    fun getWebServer() : WebServer
}