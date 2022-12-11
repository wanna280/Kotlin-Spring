package com.wanna.boot.web.server

import com.wanna.framework.context.ApplicationContext

interface WebServerApplicationContext : ApplicationContext {
    fun getWebServer(): WebServer
}