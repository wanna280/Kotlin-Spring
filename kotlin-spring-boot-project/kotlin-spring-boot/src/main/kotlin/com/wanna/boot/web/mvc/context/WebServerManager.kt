package com.wanna.boot.web.mvc.context

import com.wanna.boot.web.mvc.server.WebServerFactory
import com.wanna.boot.web.server.WebServer
import org.slf4j.LoggerFactory

/**
 * 这是一个WebServerManager，负责去启动和关闭WebServer
 *
 * @param applicationContext ApplicationContext
 * @param factory WebServerFactory
 */
open class WebServerManager(
    private val applicationContext: MvcWebServerApplicationContext,
    private val factory: WebServerFactory
) {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(WebServerManager::class.java)
    }

    // 从WebServerFactory当中去获取WebServer
    private var webServer: WebServer = factory.getWebServer()

    /**
     * 启动WebServer，并发布WebServer已经初始化完成的事件
     *
     * @see MvcWebServerInitializedEvent
     */
    open fun start() {
        try {
            this.webServer.start()  // start
        } catch (ex: Exception) {
            logger.error("启动WebServer[$webServer]失败", ex)
            throw ex
        }

        // 发布WebServer已经初始化完成事件...
        this.applicationContext.publishEvent(MvcWebServerInitializedEvent(this.webServer, this.applicationContext))
    }

    /**
     * 获取WebServer
     *
     * @return WebServer
     */
    open fun getWebServer(): WebServer = this.webServer

    /**
     * 停止WebServer的运行
     */
    open fun stop() {
        this.webServer.stop()
    }
}