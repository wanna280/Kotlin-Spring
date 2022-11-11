package com.wanna.framework.web.server.netty.server

/**
 * Netty的WebServer的底层接口
 */
interface WebServer {
    fun setPort(port: Int)
    fun getPort(): Int
    fun start()
    fun stop()
}