package com.wanna.boot.web.servlet

import com.wanna.boot.web.server.WebServer
import com.wanna.boot.web.server.WebServerFactory

/**
 * Servlet环境下的WebServerFactory
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
interface ServletWebServerFactory : WebServerFactory {

    /**
     * 根据[ServletContextInitializer]去进行初始化, 并获取到[WebServer]
     *
     * @param initializers ServletContext的初始化器
     * @return WebServer
     */
    fun getWebServer(vararg initializers: ServletContextInitializer): WebServer
}