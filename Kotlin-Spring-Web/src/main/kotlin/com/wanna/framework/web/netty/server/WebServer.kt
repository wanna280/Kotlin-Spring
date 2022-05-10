package com.wanna.framework.web.netty.server

interface WebServer {
    fun setPort(port: Int)
    fun getPort(): Int
    fun start()
    fun stop()
}