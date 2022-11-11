package com.wanna.boot.autoconfigure.web.mvc

import com.wanna.boot.web.mvc.server.WebServerFactory
import com.wanna.boot.web.server.WebServer
import com.wanna.framework.web.server.netty.server.support.NettyServer
import io.netty.channel.ChannelHandler

/**
 * Netty的WebServerFactory，负责导入一个Netty的WebServer到SpringBeanFactory当中
 *
 * @see WebServerFactory
 * @see NettyWebServer
 */
open class NettyWebServerFactory : WebServerFactory {
    private val webServer = NettyWebServer()

    override fun getWebServer() = this.webServer
    open fun setHandler(handler: ChannelHandler) = webServer.setHandler(handler)
    open fun setPort(port: Int) = webServer.nettyServer.setPort(port)

    /**
     * 设置BossGroup的线程数量
     *
     * @param nThreads 你想使用的BossGroup的线程数量
     */
    open fun setBossGroupThreads(nThreads: Int) = webServer.nettyServer.setBossGroupThreads(nThreads)

    /**
     * 设置WorkerGroup的线程数量
     *
     * @param nThreads 你想使用的Worker的线程数量
     */
    open fun setWorkerGroupThreads(nThreads: Int) = webServer.nettyServer.setWorkerGroupThreads(nThreads)

    /**
     * 基于Netty实现的WebServer，它内部包装了一个Netty的Server，
     * 将NettyServer桥接到SpringBoot的WebServer
     *
     * @param nettyServer NettyServer
     */
    open class NettyWebServer(val nettyServer: NettyServer = NettyServer()) : WebServer {
        open fun setHandler(handler: ChannelHandler) = nettyServer.setHandler(handler)
        override fun start() = nettyServer.start()
        override fun stop() = nettyServer.stop()
        override fun getPort() = nettyServer.getPort()
    }
}