package com.wanna.boot.web.mvc.server

import com.wanna.boot.web.server.WebServer

/**
 * 这是一个WebServerFactory，负责创建一个WebServer
 */
interface WebServerFactory {

    /**
     * 获取到WebServer
     *
     * @return WebServer
     */
    fun getWebServer(): WebServer
}