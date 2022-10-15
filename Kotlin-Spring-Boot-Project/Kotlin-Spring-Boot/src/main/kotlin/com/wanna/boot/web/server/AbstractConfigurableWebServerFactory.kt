package com.wanna.boot.web.server

/**
 * 这是一个抽象的可以被配置的WebServerFactory
 */
abstract class AbstractConfigurableWebServerFactory : ConfigurableWebServerFactory {

    private val port: Int = 8080

    override fun setPort(port: Int) {
        this.port
    }
}