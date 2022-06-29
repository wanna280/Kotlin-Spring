package com.wanna.boot.web.mvc.context


import com.wanna.boot.web.mvc.server.WebServerFactory
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.ApplicationContextException
import com.wanna.framework.context.support.GenericApplicationContext

/**
 * 这是一个ReactiveWebServerApplicationContext
 */
open class MvcWebServerApplicationContext(beanFactory: DefaultListableBeanFactory) :
    GenericApplicationContext(beanFactory), WebServerApplicationContext {
    // 提供一个无参数构造器
    constructor() : this(DefaultListableBeanFactory())

    // WebServerManager
    private var webServerManager: WebServerManager? = null

    override fun refresh() {
        try {
            super.refresh()
        } catch (ex: java.lang.RuntimeException) {
            webServerManager?.getWebServer()?.stop()
            throw ex
        }
    }

    /**
     * 创建WebServer，并发布ReactiveWebServerInitializedEvent事件...
     */
    override fun onRefresh() {
        super.onRefresh()
        try {
            createWebServer()
        } catch (ex: Throwable) {
            throw ApplicationContextException("不能创建ReactiveWebServer，原因是-->${ex.message}", ex)
        }
    }

    /**
     * 如果没有完成初始化的话，那么需要完成WebServer的创建
     */
    private fun createWebServer() {
        if (this.webServerManager == null) {
            val reactiveWebServerFactory = getWebServerFactory()
            this.webServerManager = WebServerManager(this, reactiveWebServerFactory)
            this.getBeanFactory()
                .registerSingleton("webServerStartStop",
                    WebServerStartStopLifecycle(this.webServerManager!!)
                )
        }
        initPropertySources()
    }

    private fun getWebServerFactory(): WebServerFactory {
        val factories = getBeanFactory().getBeansForType(WebServerFactory::class.java).values
        if (factories.isEmpty()) {
            throw ApplicationContextException("没有从容器当中去找到合适的ReactiveWebServer")
        } else if (factories.size > 1) {
            throw ApplicationContextException("从容器中找到ReactiveWebServer的数量不止1个")
        }
        return factories.iterator().next()
    }

    override fun getWebServer(): com.wanna.boot.web.server.WebServer {
        return this.webServerManager!!.getWebServer()
    }
}