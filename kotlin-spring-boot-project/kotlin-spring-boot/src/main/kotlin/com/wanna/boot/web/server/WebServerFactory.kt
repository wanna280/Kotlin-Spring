package com.wanna.boot.web.server

/**
 * 标识这是一个WebServerFactory, 是Spring对于WebServerFactory的一层抽象
 */
interface WebServerFactory {
    /**
     * 获取到WebServer的工厂方法
     *
     * @return WebServer
     */
    fun getWebServer(): WebServer
}