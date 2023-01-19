package com.wanna.boot.web.server

import com.wanna.framework.context.Lifecycle

/**
 * 这是一个负责WebServer的启动与停止的LifecycleBean
 *
 * @param webServerManager WebServerManager
 */
open class WebServerStartStopLifecycle(private val webServerManager: WebServerManager) : Lifecycle {

    /**
     * 该WebServer是否正在运行当中?
     */
    private var running = false

    /**
     * 当Lifecycle启动时, 启动WebServer
     */
    override fun start() {
        webServerManager.start()
        this.running = true
    }

    /**
     * 当Lifecycle关闭时, 关闭WebServer
     */
    override fun stop() {
        webServerManager.stop()
        this.running = false
    }

    override fun isRunning() = this.running
    override fun toString() = this.running.toString()
}