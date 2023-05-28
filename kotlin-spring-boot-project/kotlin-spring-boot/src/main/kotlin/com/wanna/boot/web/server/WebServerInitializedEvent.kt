package com.wanna.boot.web.server

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.event.ApplicationEvent

/**
 * WebServer已经完成初始化的事件
 *
 * @param webServer WebServer
 */
abstract class WebServerInitializedEvent(val webServer: WebServer) : ApplicationEvent(webServer) {

    /**
     * 获取到发布事件的ApplicationContext
     *
     * @return ApplicationContext
     */
    abstract fun getApplicationContext(): ApplicationContext
}