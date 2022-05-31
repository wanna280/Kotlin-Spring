package com.wanna.boot.autoconfigure.web.mvc

import com.wanna.boot.web.reactive.server.ReactiveWebServerFactory
import com.wanna.boot.web.server.WebServer
import com.wanna.framework.web.server.netty.server.support.NettyServer
import io.netty.channel.ChannelHandler

open class NettyWebServerFactory : ReactiveWebServerFactory {
    private val webServer = NettyWebServer()

    override fun getWebServer(): WebServer {
        return webServer
    }

    open fun setHandler(handler: ChannelHandler) {
        webServer.setHandler(handler)
    }

    open fun setPort(port: Int) {
        webServer.nettyServer.setPort(port)
    }

    open class NettyWebServer : WebServer {
        val nettyServer = NettyServer()

        open fun setHandler(handler: ChannelHandler) {
            nettyServer.setHandler(handler)
        }

        override fun start() {
            nettyServer.start()
        }

        override fun stop() {
            nettyServer.stop()
        }

        override fun getPort(): Int {
            return nettyServer.getPort()
        }
    }
}