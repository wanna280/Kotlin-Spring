package com.wanna.boot.web.server

import com.wanna.boot.web.mvc.context.MvcWebServerInitializedEvent
import com.wanna.framework.context.ApplicationContext
import org.slf4j.LoggerFactory

/**
 * 这是一个WebServerManager, 负责去启动和关闭WebServer
 *
 * @param applicationContext ApplicationContext
 * @param webServer WebServer
 */
open class WebServerManager(private val applicationContext: ApplicationContext, private val webServer: WebServer) {

    companion object {

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(WebServerManager::class.java)
    }

    /**
     * 启动WebServer, 并发布WebServer已经初始化完成的事件
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