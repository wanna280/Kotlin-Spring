package com.wanna.boot.web.mvc.context

import com.wanna.framework.context.Lifecycle

/**
 * 这是一个负责WebServer的启动与停止的LifecycleBean
 *
 * @param webServerManager WebServerManager
 */
open class WebServerStartStopLifecycle(private val webServerManager: WebServerManager) : Lifecycle {

    // 该WebServer是否正在运行当中？
    private var running = false

    override fun start() {
        webServerManager.start()
        this.running = true
    }

    override fun stop() {
        webServerManager.stop()
        this.running = false
    }

    override fun isRunning() = this.running
    override fun toString() = this.running.toString()
}