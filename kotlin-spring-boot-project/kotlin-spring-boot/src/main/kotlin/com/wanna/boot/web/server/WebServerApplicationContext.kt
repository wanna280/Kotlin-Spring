package com.wanna.boot.web.server

import com.wanna.framework.context.ApplicationContext

/**
 * 支持去获取到WebServer的ApplicationContext
 *
 * @see WebServer
 * @see ApplicationContext
 */
interface WebServerApplicationContext : ApplicationContext {

    /**
     * 获取到WebServer
     *
     * @return WebServer
     */
    fun getWebServer(): WebServer
}