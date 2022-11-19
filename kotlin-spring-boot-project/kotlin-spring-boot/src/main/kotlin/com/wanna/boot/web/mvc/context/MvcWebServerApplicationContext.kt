package com.wanna.boot.web.mvc.context


import com.wanna.boot.web.mvc.server.WebServerFactory
import com.wanna.boot.web.server.WebServer
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.ApplicationContextException
import com.wanna.framework.context.support.GenericApplicationContext

/**
 * 这是一个Mvc的WebServerApplicationContext
 *
 * @param beanFactory ApplicationContext需要使用的BeanFactory
 */
open class MvcWebServerApplicationContext(beanFactory: DefaultListableBeanFactory) :
    GenericApplicationContext(beanFactory), WebServerApplicationContext {
    /**
     * 提供一个无参数构造器
     */
    constructor() : this(DefaultListableBeanFactory())

    /**
     * WebServerManager
     */
    private var webServerManager: WebServerManager? = null

    /**
     * WebServer
     */
    private var webServer: WebServer? = null

    /**
     * 重写父类的refresh方法，当刷新SpringBeanFactory出现异常时，我们需要去关闭WebServer
     *
     * @see WebServerManager.webServer
     * @see WebServer.stop
     */
    override fun refresh() {
        try {
            super.refresh()
        } catch (ex: java.lang.RuntimeException) {
            webServerManager?.getWebServer()?.stop()
            throw ex
        }
    }

    /**
     * 创建WebServer，并发布MvcWebServerInitializedEvent事件...
     *
     * @see MvcWebServerInitializedEvent
     */
    override fun onRefresh() {
        super.onRefresh()
        try {
            createWebServer()
        } catch (ex: Throwable) {
            throw ApplicationContextException("创建MvcWebServer时出现异常", ex)
        }
    }

    /**
     * 如果没有完成初始化的话，那么需要完成WebServer的创建
     */
    private fun createWebServer() {
        if (this.webServerManager == null) {
            val webServerFactory = getWebServerFactory()

            val step = this.getApplicationStartup().start("spring.boot.webserver.create")
            step.tag("factory", webServerFactory::class.java.name) // tag
            this.webServer = webServerFactory.getWebServer()  // get WebServer
            step.end()  // tag end

            // 创建WebServerManager
            this.webServerManager = WebServerManager(this, webServerFactory)

            // 注册一个WebServer的启动/暂停的Lifecycle的Bean到BeanFactory当中
            this.getBeanFactory().registerSingleton(
                "webServerStartStop",
                WebServerStartStopLifecycle(this.webServerManager!!)
            )
        }

        // initPropertySources
        initPropertySources()
    }

    /**
     * 从Spring BeanFactory当中去探测WebServerFactory。
     * 如果探测到一个，直接返回；如果探测到多个/没有探测到WebServerFactory，都需要抛出异常。
     *
     * @return 探测到的WebServerFactory
     */
    private fun getWebServerFactory(): WebServerFactory {
        val factoryNames = getBeanFactory().getBeanNamesForType(WebServerFactory::class.java)
        return when (factoryNames.size) {
            0 -> throw ApplicationContextException("没有从BeanFactory当中去找到合适的[${WebServerFactory::class.java}]类型的Bean")
            1 -> getBeanFactory().getBean(factoryNames[0], WebServerFactory::class.java)
            else -> throw ApplicationContextException("从BeanFactory中找到WebServerFactory的数量不止1个, 包含有下面这样的几个:$factoryNames")
        }
    }

    /**
     * 获取WebServer
     *
     * @return WebServer
     */
    override fun getWebServer(): WebServer = this.webServer ?: throw IllegalStateException("无法获取到WebServer")
}