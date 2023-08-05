package com.wanna.boot.web.server

/**
 * 这是一个WebServerFactory的自定义化器, 支持去对WebServer去进行自定义
 *
 * @see WebServerFactory
 * @see WebServer
 */
interface WebServerFactoryCustomizer<C : WebServerFactory> {
    fun customize(factory: C)
}