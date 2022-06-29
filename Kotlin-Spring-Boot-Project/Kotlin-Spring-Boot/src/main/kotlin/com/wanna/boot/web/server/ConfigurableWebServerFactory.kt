package com.wanna.boot.web.server

/**
 * 这是可以去进行配置的WebServerFactory
 */
interface ConfigurableWebServerFactory {
    /**
     * 设置WebServer的端口号
     */
    fun setPort(port: Int)
}