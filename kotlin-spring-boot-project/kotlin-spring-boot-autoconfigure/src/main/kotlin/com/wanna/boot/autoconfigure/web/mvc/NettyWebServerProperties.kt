package com.wanna.boot.autoconfigure.web.mvc

import com.wanna.boot.context.properties.ConfigurationProperties

/**
 * 针对于NettyWebServer的配置属性, 绑定"server"作为前缀
 */
@ConfigurationProperties("server")
open class NettyWebServerProperties {

    /**
     * serverPort
     */
    var port: Int = 9966

    /**
     * bossGroupCount
     */
    var bossCount = 1

    /**
     * workerCount
     */
    var workerCount = Runtime.getRuntime().availableProcessors() * 2
}