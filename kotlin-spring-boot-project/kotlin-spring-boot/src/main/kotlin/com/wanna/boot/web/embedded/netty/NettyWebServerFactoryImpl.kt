package com.wanna.boot.web.embedded.netty

import com.wanna.boot.web.mvc.server.NettyWebServerFactory
import io.netty.channel.ChannelHandler

/**
 * Netty的WebServerFactory，负责导入一个Netty的WebServer到SpringBeanFactory当中
 *
 * @see NettyWebServer
 */
open class NettyWebServerFactoryImpl : NettyWebServerFactory {

    /**
     * NettyWebServer
     */
    private val webServer = NettyWebServer()

    /**
     * 获取NettyWebServer
     *
     * @return NettyWebServerFactoryImpl
     */
    override fun getWebServer() = this.webServer

    /**
     * 设置NettyServerHandler
     *
     * @param handler handler
     */
    open fun setHandler(handler: ChannelHandler) = webServer.setHandler(handler)

    /**
     * 设置NettyServer的启动端口号
     */
    open fun setPort(port: Int) = webServer.setPort(port)

    /**
     * 设置BossGroup的线程数量
     *
     * @param nThreads 你想使用的BossGroup的线程数量
     */
    open fun setBossGroupThreads(nThreads: Int) = webServer.setBossGroupThreads(nThreads)

    /**
     * 设置WorkerGroup的线程数量
     *
     * @param nThreads 你想使用的Worker的线程数量
     */
    open fun setWorkerGroupThreads(nThreads: Int) = webServer.setWorkerGroupThreads(nThreads)
}