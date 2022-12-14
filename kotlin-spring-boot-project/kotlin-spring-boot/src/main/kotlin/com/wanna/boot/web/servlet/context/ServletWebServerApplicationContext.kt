package com.wanna.boot.web.servlet.context

import com.wanna.boot.web.server.WebServer
import com.wanna.boot.web.server.WebServerApplicationContext
import com.wanna.boot.web.server.WebServerManager
import com.wanna.boot.web.server.WebServerStartStopLifecycle
import com.wanna.boot.web.servlet.ServletContextInitializer
import com.wanna.boot.web.servlet.ServletWebServerFactory
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.ApplicationContextException
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils.getQualifiedName
import com.wanna.framework.web.context.ConfigurableWebApplicationContext
import com.wanna.framework.web.context.ConfigurableWebEnvironment
import com.wanna.framework.web.context.WebApplicationContext
import com.wanna.framework.web.context.support.GenericWebApplicationContext
import com.wanna.framework.web.context.support.StandardServletEnvironment
import com.wanna.framework.web.context.support.WebApplicationContextUtils
import org.slf4j.LoggerFactory
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

/**
 *
 * 这是一个Servlet环境下的WebServerApplicationContext
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 *
 * @param beanFactory ApplicationContext需要使用的BeanFactory
 * @param servletContext ServletContext
 */
open class ServletWebServerApplicationContext(
    @Nullable servletContext: ServletContext? = null,
    beanFactory: DefaultListableBeanFactory
) : GenericWebApplicationContext(servletContext, beanFactory), WebServerApplicationContext,
    ConfigurableWebApplicationContext {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ServletWebServerApplicationContext::class.java)
    }

    /**
     * 提供一个无参数构造器, 对于BeanFactory采用默认的BeanFactory
     */
    constructor() : this(null, DefaultListableBeanFactory())

    /**
     * 提供一个基于ServletContext的构造器
     *
     * @param servletContext ServletContext
     */
    constructor(@Nullable servletContext: ServletContext?) : this(servletContext, DefaultListableBeanFactory())

    /**
     * 提供一个基于BeanFactory的构造器
     *
     * @param beanFactory BeanFactory
     */
    constructor(beanFactory: DefaultListableBeanFactory) : this(null, beanFactory)

    /**
     * WebServerManager
     */
    @Nullable
    private var webServerManager: WebServerManager? = null

    /**
     * WebServer
     */
    @Volatile
    @Nullable
    private var webServer: WebServer? = null

    /**
     * ServletConfig
     */
    @Nullable
    private var servletConfig: ServletConfig? = null

    /**
     * set ServletConfig
     *
     * @param servletConfig ServletConfig
     */
    override fun setServletConfig(@Nullable servletConfig: ServletConfig?) {
        this.servletConfig = servletConfig
    }

    /**
     * get ServletConfig
     *
     * @return ServletConfig
     */
    @Nullable
    override fun getServletConfig(): ServletConfig? = this.servletConfig

    /**
     * 在BeanFactory完成初始化之后, 就往BeanFactory当中去添加一个处理ServletContextAware的BeanPostProcessor, 保证优先级足够高
     *
     * @param beanFactory BeanFactory
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        beanFactory.addBeanPostProcessor(WebApplicationContextServletContextAwareProcessor(this))
    }

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
     * 创建ServletWebServer，并发布ServletWebServerInitializedEvent事件...
     *
     * @see com.wanna.boot.web.server.WebServerInitializedEvent
     * @see ServletWebServerInitializedEvent
     */
    override fun onRefresh() {
        super.onRefresh()
        try {
            createWebServer()
        } catch (ex: Throwable) {
            throw ApplicationContextException("create Servlet WebServer Error", ex)
        }
    }

    /**
     * 如果之前WebServer还未完成初始化的话, 那么先启动Servlet的WebServer, 并利用ServletContext完成初始化;
     * 如果之前就是通过WebServer(如Tomcat)启动起来的话, 那么需要根据ServletContext去完成自定义的初始化.
     *
     * 这里的初始化包含了很多部分的功能点: 最重要的一个点在于, RegistrationBean尝试去注册Servlet/Filter/Listener到ServletContext当中去
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

            // 如果之前就已经存在有ServletContext的话, 说明是通过Tomcat(or Other)容器启动起来的...需要根据ServletContext去进行初始化
        } else if (servletContext != null) {
            try {
                getSelfInitializer().onStartup(servletContext)
            } catch (ex: Exception) {
                throw ApplicationContextException("Cannot initialize ServletContext", ex)
            }
        }

        // 如果之前已经有ServletContext了的话, 那么根据ServletContext/ServletConfig的initParameters去初始化PropertySources
        initPropertySources()
    }

    /**
     * 获取Self的Initializer, 去根据ServletContext去进行初始化当前的ApplicationContext.
     * Note: 将Servlet/Filter注册到Tomcat当中的这个流程, 其实就是通过ServletContextInitializer去完成的.
     *
     * @return 执行ApplicationContext自我初始化的ServletContextInitializer
     */
    private fun getSelfInitializer(): ServletContextInitializer {
        return ServletContextInitializer { selfInitialize(it) }
    }

    /**
     * 根据ServletContext, 去执行当前的WebServer的ApplicationContext内部的初始化
     *
     * @param servletContext ServletContext
     */
    private fun selfInitialize(servletContext: ServletContext) {
        // 准备WebApplicationContext, 将当前ApplicationContext去设置为root, 并将ServletContext去保存到当前ApplicationContext当中
        prepareWebApplicationContext(servletContext)

        // 将ServletContext作为Bean注册到BeanFactory当中, 将ServletContext的ContextParameters&ContextAttributes作为Bean注册到BeanFactory当中
        WebApplicationContextUtils.registerEnvironmentBeans(getBeanFactory(), servletContext)

        // 获取到BeanFactory当中的所有的ServletContext的Initializer的Bean, 去根据ServletContext去执行初始化
        for (initializer in getServletContextInitializerBeans()) {
            initializer.onStartup(servletContext)
        }
    }

    /**
     * 根据给定的ServletContext, 去准备当前的WebApplicationContext.
     *
     * * 1.因为当前是一个WebServer的ApplicationContext, 那么代表要使用一个嵌入式的WebServer去启动应用,
     * 因此需要将当前ApplicationContext设置为root, 将它填充到ServletContext的属性当中
     * * 2.setServletContext, 将给定的ServletContext设置到当前的ApplicationContext当中去
     *
     * @param servletContext ServletContext
     * @see setServletContext
     * @throws IllegalStateException 如果ServletContext当中已经存在有一个RootApplicationContext的属性, 或者出现了一些其他的异常
     */
    @Throws(Throwable::class)
    protected open fun prepareWebApplicationContext(servletContext: ServletContext) {
        // 检查之前是否已经设置过root?
        val rootContext = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)
        if (rootContext != null) {
            throw IllegalStateException("在ServletContext当中已经存在有一个RootApplicationContext!检查是否有重复添加ServletContextInitializer!")
        }
        // log Root WebApplicationContext info
        servletContext.log("Initializing Spring embedded WebApplicationContext")
        try {
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this)

            if (logger.isDebugEnabled) {
                logger.debug("Published root WebApplicationContext as ServletContext attribute with name [${WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE}]")
            }

            // 将ServletContext保存到当前ApplicationContext当中...
            setServletContext(servletContext)

            // log elapsedTime
            if (logger.isInfoEnabled) {
                logger.info("Root WebApplicationContext: initialization completed in ${System.currentTimeMillis() - getStartupDate()} ms")
            }

        } catch (ex: Throwable) {
            logger.error("Context initialization failed", ex)
            // 将RootApplicationContext的属性去设置成为丢出来的异常Error
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex)
            throw ex  // rethrow
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
     * 从Spring BeanFactory当中去探测ServletWebServerFactory.
     * 如果探测到一个，直接返回；如果探测到多个/没有探测到WebServerFactory，都需要抛出异常。
     *
     * @return 探测到的WebServerFactory(不会为null, 万一出现了什么情况, 都是直接就丢出去异常)
     * @throws ApplicationContextException 如果BeanFactory当中没有合适的ServletWebServerFactory/存在有超过一个ServletWebServerFactory
     */
    @Throws(ApplicationContextException::class)
    private fun getWebServerFactory(): ServletWebServerFactory {
        val factoryNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory::class.java)
        return when (factoryNames.size) {
            0 -> throw ApplicationContextException("没有从BeanFactory当中去找到合适的[${getQualifiedName(ServletWebServerFactory::class.java)}]类型的Bean")
            1 -> getBeanFactory().getBean(factoryNames[0], ServletWebServerFactory::class.java)
            else -> throw ApplicationContextException("从BeanFactory中找到WebServerFactory的数量不止1个, 包含有下面这样的几个:$factoryNames")
        }
    }

    /**
     * 重写这个钩子方法, 尝试根据ServletContext/ServletConfig去初始化PropertySources.
     * 如果是使用的嵌入式的WebServer, 那么这里ServletContext/ServletConfig都还没初始化, 因此这里不会有作用.
     * 如果是使用的Tomcat去进行的启动的话, 那么这里ServletContext已经完成初始化, 这里就可以对PropertySources去执行初始化
     */
    override fun initPropertySources() {
        val environment = getEnvironment()
        if (environment is ConfigurableWebEnvironment) {
            environment.initPropertySources(getServletContext(), getServletConfig())
        }
    }

    /**
     * 重写父类的钩子方法, 创建Environment时我们应该去创建一个StandardServletEnvironment
     *
     * @return StandServletEnvironment
     */
    override fun createEnvironment(): ConfigurableWebEnvironment = StandardServletEnvironment()

    /**
     * 获取WebServer
     *
     * @return WebServer
     */
    override fun getWebServer(): WebServer =
        this.webServer ?: throw IllegalStateException("无法获取到WebServer, 请先完成WebServer的初始化")
}