package com.wanna.framework.web.netty.server.support


import com.wanna.framework.web.netty.server.WebServer
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 这是一个基于Netty去进行实现的HttpServer
 */
open class NettyServer : WebServer {
    private val logger: Logger = LoggerFactory.getLogger(NettyServer::class.java)

    private var port = 9966
    private val bossGroup: EventLoopGroup = NioEventLoopGroup(1)
    private val workerGroup: EventLoopGroup = NioEventLoopGroup()
    private val serverBootstrap: ServerBootstrap = ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel::class.java)
        .childOption(ChannelOption.SO_KEEPALIVE, true)

    /**
     * 自定义ChannelInitializer
     *
     * @param initializer 要配置的ChannelInitializer
     */
    open fun setInitializer(initializer: ChannelInitializer<SocketChannel>) {
        serverBootstrap.childHandler(initializer)
    }

    /**
     * 自定义处理请求的ChannelHandler
     *
     * @param handler 要配置的Handler
     */
    open fun setHandler(handler: ChannelHandler) {
        serverBootstrap.childHandler(NettyHttpServerInitializer(handler))
    }

    override fun setPort(port: Int) {
        this.port = port
    }

    override fun getPort(): Int {
        return this.port
    }

    override fun start() {
        serverBootstrap.bind(this.port).sync()
        logger.info("Netty Server已经启动在[${this.port}]端口")
    }

    override fun stop() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}