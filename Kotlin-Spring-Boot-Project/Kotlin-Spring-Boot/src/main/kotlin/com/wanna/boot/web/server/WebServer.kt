package com.wanna.boot.web.server

/**
 * 这是Spring对于WebServer的一层抽象，下面可以有各种类型的WebServer，比如Tomcat/Netty等
 */
interface WebServer {
    /**
     * 启动WebServer
     */
    fun start()

    /**
     * 关闭WebServer
     */
    fun stop()

    /**
     * 获取WebServer的端口
     */
    fun getPort(): Int
}