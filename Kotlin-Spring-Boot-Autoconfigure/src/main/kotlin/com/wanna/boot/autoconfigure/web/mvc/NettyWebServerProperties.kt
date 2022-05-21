package com.wanna.boot.autoconfigure.web.mvc

import com.wanna.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("server")
open class NettyWebServerProperties {
    var port: Int = 9966
}