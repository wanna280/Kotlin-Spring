package com.wanna.boot.web.mvc.context

import com.wanna.framework.context.Lifecycle

/**
 * 这是一个负责WebServer的启动与停止的Lifecycle
 */
open class WebServerStartStopLifecycle(private val webServerManager: WebServerManager) : Lifecycle {
    private var running = false

    override fun start() {
        webServerManager.start()
        this.running = true
    }

    override fun stop() {
        webServerManager.stop()
        this.running = false
    }

    override fun isRunning(): Boolean {
        return this.running
    }
}