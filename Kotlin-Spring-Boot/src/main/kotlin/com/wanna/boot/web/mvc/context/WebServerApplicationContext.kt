package com.wanna.boot.web.mvc.context

import com.wanna.boot.web.server.WebServer
import com.wanna.framework.context.ApplicationContext

interface WebServerApplicationContext : ApplicationContext {
    fun getWebServer() : WebServer
}