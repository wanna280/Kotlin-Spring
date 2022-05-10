package com.wanna.boot.autoconfigure.web.reactive

import com.wanna.boot.web.reactive.server.ReactiveWebServerFactory
import com.wanna.boot.web.server.WebServer
import com.wanna.framework.web.netty.server.support.NettyServer
import io.netty.channel.ChannelHandler

class NettyWebServerFactory : ReactiveWebServerFactory {
    private val webServer = NettyWebServer()

    override fun getWebServer(): WebServer {
        return webServer
    }

    fun setHandler(handler: ChannelHandler) {
        webServer.setHandler(handler)
    }

    class NettyWebServer : WebServer {
        private val nettyServer = NettyServer()

        fun setHandler(handler: ChannelHandler) {
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