package com.wanna.boot.web.embedded.netty


import com.wanna.boot.web.server.WebServer
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory

/**
 * 这是一个基于Netty去进行实现的HttpServer
 *
 * @see WebServer
 */
open class NettyWebServer : WebServer {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger: Logger = LoggerFactory.getLogger(NettyWebServer::class.java)

        /**
         * 默认端口
         */
        const val DEFAULT_SERVER_PORT = 9966

        /**
         * 默认的Boss数量
         */
        const val DEFAULT_BOSS_GROUP_THREADS = 1

        /**
         * 默认的Worker的数量
         */
        @JvmStatic
        val DEFAULT_WORKER_GROUP_THREADS = Runtime.getRuntime().availableProcessors() * 2
    }

    private var port = DEFAULT_SERVER_PORT
    private var bossGroup: EventLoopGroup = NioEventLoopGroup(DEFAULT_BOSS_GROUP_THREADS)
    private var workerGroup: EventLoopGroup = NioEventLoopGroup(DEFAULT_WORKER_GROUP_THREADS)
    private val serverBootstrap: ServerBootstrap = ServerBootstrap()

    /**
     * 自定义ChannelInitializer
     *
     * @param initializer 要配置的ChannelInitializer
     */
    open fun setInitializer(initializer: ChannelInitializer<SocketChannel>) {
        serverBootstrap.childHandler(initializer)
    }

    /**
     * 设置BossGroup的线程数量(默认为1)
     *
     * @param nThreads Boss线程数量
     */
    open fun setBossGroupThreads(nThreads: Int) {
        this.bossGroup = NioEventLoopGroup(nThreads)
    }

    /**
     * 设置Worker线程的数量(默认为可用CPU核数的两倍)
     *
     * @param nThreads Worker线程数量
     */
    open fun setWorkerGroupThreads(nThreads: Int) {
        this.workerGroup = NioEventLoopGroup(nThreads)
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

    override fun getPort() = this.port

    override fun start() {
        initServerBootstrap()  // init ServerBootstrap
        serverBootstrap.bind(this.port).sync()  // sync
        logger.info("Netty Web Server在[${this.port}]端口启动")
    }

    private fun initServerBootstrap() {
        serverBootstrap
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.SO_REUSEADDR, true)
    }

    override fun stop() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}