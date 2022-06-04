package com.wanna.boot.web.mvc.server

import com.wanna.boot.web.server.WebServer

/**
 * 这是一个Reactive的WebServerFactory
 */
interface WebServerFactory {
    fun getWebServer() : WebServer
}