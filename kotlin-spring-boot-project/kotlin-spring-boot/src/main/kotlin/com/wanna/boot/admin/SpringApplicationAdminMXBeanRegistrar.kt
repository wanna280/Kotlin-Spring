package com.wanna.boot.admin

import com.wanna.boot.admin.SpringApplicationAdminMXBeanRegistrar.SpringApplicationAdmin
import com.wanna.boot.context.event.ApplicationReadyEvent
import com.wanna.boot.web.server.WebServerInitializedEvent
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.GenericApplicationListener
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.common.logging.LoggerFactory
import java.lang.management.ManagementFactory
import javax.management.ObjectName

/**
 * SpringBoot Admin的MXBean的注册器, 负责通过JMX去注册一个MXBean, 去对SpringBoot进行管理和监控
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 *
 * @see SpringApplicationAdmin
 * @see SpringApplicationAdminMXBean
 */
open class SpringApplicationAdminMXBeanRegistrar(objectName: String) : ApplicationContextAware, EnvironmentAware,
    InitializingBean, DisposableBean, GenericApplicationListener {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(SpringApplicationAdminMXBeanRegistrar::class.java)
    }

    /**
     * JMX MBean ObjectName
     */
    private val objectName = ObjectName(objectName)

    /**
     * ApplicationContext
     */
    private var applicationContext: ConfigurableApplicationContext? = null

    /**
     * Environment
     */
    private var environment: Environment = StandardEnvironment()

    /**
     * 当前ApplicationContext是否已经ready?
     *
     * @see ApplicationReadyEvent
     */
    private var ready = false

    /**
     * 当前是否是一个嵌入式的Web应用?
     *
     * @see WebServerInitializedEvent
     */
    private var embeddedWebApplication = false

    /**
     * 自动注册[ApplicationContext]
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        if (applicationContext !is ConfigurableApplicationContext) {
            throw IllegalStateException("ApplicationContext does not implement ConfigurableApplicationContext")
        }
        this.applicationContext = applicationContext
    }

    /**
     * 注入[Environment]
     *
     * @param environment Environment
     */
    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    /**
     * 处理[ApplicationReadyEvent]和[WebServerInitializedEvent]这两个[ApplicationEvent]事件
     *
     * @param event event
     */
    override fun onApplicationEvent(event: ApplicationEvent) {
        if (event is ApplicationReadyEvent) {
            onApplicationReadyEvent(event)
        }
        if (event is WebServerInitializedEvent) {
            onWebServerInitializedEvent(event)
        }
    }

    /**
     * 当接收到[ApplicationReadyEvent]事件时, 说明Spring应用已经就绪了
     *
     * @param event ApplicationReadyEvent
     */
    private fun onApplicationReadyEvent(event: ApplicationReadyEvent) {
        if (this.applicationContext == event.context) {
            this.ready = true
        }
    }

    /**
     * 当接收到[WebServerInitializedEvent]事件时, 说明是使用嵌入式容器去进行启动的
     *
     * @param event WebServerInitializedEvent
     */
    private fun onWebServerInitializedEvent(event: WebServerInitializedEvent) {
        if (this.applicationContext == event.getApplicationContext()) {
            this.embeddedWebApplication = true
        }
    }

    /**
     * 支持去进行处理的事件类型, 我们主要监听[ApplicationReadyEvent]和[WebServerInitializedEvent]事件
     *
     * @param type 事件类型
     * @return 如果是[ApplicationReadyEvent]或者是[WebServerInitializedEvent]的话, return true; 否则return false
     */
    override fun supportsEventType(type: ResolvableType): Boolean {
        val rawType = type.getRawClass() ?: return false
        return ClassUtils.isAssignFrom(ApplicationReadyEvent::class.java, rawType)
                || ClassUtils.isAssignFrom(WebServerInitializedEvent::class.java, rawType)
    }

    /**
     * Listener的优先级, 设置为最高
     *
     * @return order of listener
     */
    override fun getOrder(): Int = Ordered.ORDER_HIGHEST

    /**
     * 当这个Bean初始化时, 将[SpringApplicationAdmin]去注册到MBeanServer当中
     */
    override fun afterPropertiesSet() {
        ManagementFactory.getPlatformMBeanServer().registerMBean(SpringApplicationAdmin(), this.objectName)
        if (logger.isDebugEnabled) {
            logger.debug("Application Admin MBean registered with name '$objectName'")
        }
    }

    /**
     * 当这个Bean被destroy时, 将[SpringApplicationAdmin]去从MBeanServer当中移除掉
     */
    override fun destroy() {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.objectName)
    }

    private inner class SpringApplicationAdmin : SpringApplicationAdminMXBean {

        override fun isReady(): Boolean = ready

        override fun isEmbeddedWebApplication(): Boolean = embeddedWebApplication

        @Nullable
        override fun getProperty(key: String): String? = environment.getProperty(key)

        override fun shutdown() {
            logger.info("Application shutdown requested.")
            applicationContext?.close()
        }
    }
}