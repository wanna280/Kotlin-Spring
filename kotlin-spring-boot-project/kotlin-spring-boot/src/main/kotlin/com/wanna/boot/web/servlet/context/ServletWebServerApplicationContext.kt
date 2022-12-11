package com.wanna.boot.web.servlet.context

import com.wanna.boot.web.mvc.server.NettyWebServerFactory
import com.wanna.boot.web.server.WebServer
import com.wanna.boot.web.server.WebServerApplicationContext
import com.wanna.boot.web.server.WebServerManager
import com.wanna.boot.web.server.WebServerStartStopLifecycle
import com.wanna.boot.web.servlet.ServletContextInitializer
import com.wanna.boot.web.servlet.ServletWebServerFactory
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.ApplicationContextException
import com.wanna.framework.context.support.GenericApplicationContext
import com.wanna.framework.lang.Nullable
import javax.servlet.ServletContext
import kotlin.jvm.Throws

/**
 *
 * 这是一个Servlet环境下的WebServerApplicationContext
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 *
 * @param beanFactory ApplicationContext需要使用的BeanFactory
 */
open class ServletWebServerApplicationContext(beanFactory: DefaultListableBeanFactory) :
    GenericApplicationContext(beanFactory), WebServerApplicationContext {
    /**
     * 提供一个无参数构造器
     */
    constructor() : this(DefaultListableBeanFactory())

    /**
     * WebServerManager
     */
    @Nullable
    private var webServerManager: WebServerManager? = null

    /**
     * WebServer
     */
    @Nullable
    private var webServer: WebServer? = null

    /**
     * ServletContext
     */
    @Nullable
    private var servletContext: ServletContext? = null

    /**
     * 设置ServletContext
     *
     * @param servletContext ServletContext
     */
    open fun setServletContext(servletContext: ServletContext) {
        this.servletContext = servletContext
    }

    /**
     * 获取ServletContext
     *
     * @return ServletContext
     */
    @Nullable
    open fun getServletContext(): ServletContext? = this.servletContext

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
     * 创建WebServer，并发布ServletWebServerInitializedEvent事件...
     *
     * @see com.wanna.boot.web.server.WebServerInitializedEvent
     * @see ServletWebServerInitializedEvent
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
        val servletContext = getServletContext()
        // 如果WebServerManager&ServletContext都为空的话, 那么就需要使用WebServerFactory去创建出来嵌入式的WebServer
        if (this.webServerManager == null && servletContext == null) {
            val step = this.getApplicationStartup().start("spring.boot.webserver.create")
            val webServerFactory = getWebServerFactory()
            step.tag("factory", webServerFactory::class.java.name) // tag

            // 根据WebServerFactory, 去创建出来WebServer, 并使用ServletContextInitializer去完成初始化...
            this.webServer = webServerFactory.getWebServer(getSelfInitializer())  // get WebServer
            step.end()  // tag end

            // 创建WebServerManager
            this.webServerManager = WebServerManager(this, getWebServer())

            // 注册一个WebServer的启动/暂停的Lifecycle的Bean到BeanFactory当中
            this.getBeanFactory().registerSingleton(
                "webServerStartStop",
                WebServerStartStopLifecycle(this.webServerManager!!)
            )

            // 如果之前就已经存在有ServletContext的话, 说明是通过Tomcat启动起来的...需要根据ServletContext去进行初始化
        } else if (servletContext != null) {
            try {
                getSelfInitializer().onStartup(servletContext)
            } catch (ex: Exception) {
                throw ApplicationContextException("Cannot initialize ServletContext", ex)
            }
        }

        // initPropertySources
        initPropertySources()
    }

    /**
     * 获取Self的Initializer, 去根据ServletContext去进行初始化当前的ApplicationContext
     *
     * @return ServletContextInitializer
     */
    private fun getSelfInitializer(): ServletContextInitializer {
        return ServletContextInitializer { selfInitialize(it) }
    }

    /**
     * 根据ServletContext, 去执行自我的初始化
     *
     * @param servletContext ServletContext
     */
    private fun selfInitialize(servletContext: ServletContext) {
        // 获取到所有的ServletContext的InitializerBean, 去执行初始化
        for (initializer in getServletContextInitializerBeans()) {
            initializer.onStartup(servletContext)
        }
    }

    /**
     * 获取BeanFactory当中的所有的[ServletContextInitializer]的Bean
     *
     * @return ServletContextInitializer Beans
     */
    protected open fun getServletContextInitializerBeans(): Collection<ServletContextInitializer> {
        return getBeanFactory().getBeansForType(ServletContextInitializer::class.java).values
    }

    /**
     * 从Spring BeanFactory当中去探测WebServerFactory.
     * 如果探测到一个，直接返回；如果探测到多个/没有探测到WebServerFactory，都需要抛出异常。
     *
     * @return 探测到的WebServerFactory
     * @throws ApplicationContextException 如果BeanFactory当中没有合适的WebServerFactory/存在有超过一个WebServerFactory
     */
    @Throws(ApplicationContextException::class)
    private fun getWebServerFactory(): ServletWebServerFactory {
        val factoryNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory::class.java)
        return when (factoryNames.size) {
            0 -> throw ApplicationContextException("没有从BeanFactory当中去找到合适的[${ServletWebServerFactory::class.java}]类型的Bean")
            1 -> getBeanFactory().getBean(factoryNames[0], ServletWebServerFactory::class.java)
            else -> throw ApplicationContextException("从BeanFactory中找到WebServerFactory的数量不止1个, 包含有下面这样的几个:$factoryNames")
        }
    }


    /**
     * 获取WebServer
     *
     * @return WebServer
     */
    override fun getWebServer(): WebServer = this.webServer ?: throw IllegalStateException("无法获取到WebServer, 请先完成初始化")
}