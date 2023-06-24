package com.wanna.cloud.client.serviceregistry

import com.wanna.boot.web.server.WebServerInitializedEvent
import com.wanna.cloud.client.discovery.event.InstancePreRegisteredEvent
import com.wanna.cloud.client.discovery.event.InstanceRegisteredEvent
import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.core.environment.Environment
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.PreDestroy

/**
 * 为服务的自动注册提供抽象的模板方法实现, 它组合了[ServiceRegistry], 去完成服务的**自动注册**
 *
 * @see start
 * @see stop
 *
 * @param registry SpringCloud服务的注册中心, 当前服务要去注册到哪个注册中心当中去
 * @param properties SpringCloud服务注册的相关配置信息
 */
abstract class AbstractAutoServiceRegistration<R : Registration>(
    private val registry: ServiceRegistry<R>,
    private val properties: AutoServiceRegistrationProperties
) : AutoServiceRegistration, ApplicationContextAware, ApplicationListener<WebServerInitializedEvent> {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(AbstractAutoServiceRegistration::class.java)
    }

    /**
     * 服务是否已经完成注册的标志位
     */
    private val running: AtomicBoolean = AtomicBoolean(false)

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    /**
     * Environment
     */
    private var environment: Environment? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        this.environment = applicationContext.getEnvironment()
    }

    protected open fun getApplicationContext(): ApplicationContext {
        return this.applicationContext ?: throw IllegalStateException("ApplicationContext has not initialized")
    }

    protected open fun getEnvironment(): Environment {
        return this.environment ?: throw IllegalStateException("Environment has not initialized")
    }

    /**
     * 在发布了WebServer初始化完成事件之后, 自动注册Service到注册中心当中;
     * 这个事件会在Spring ApplicationContext启动时的onRefresh步骤当中去进行发布...
     *
     * @param event event
     */
    override fun onApplicationEvent(event: WebServerInitializedEvent) {
        this.start()
    }

    /**
     * 当destroy时, 执行deregister取消服务的注册
     */
    @PreDestroy
    open fun destroy() {
        stop()
    }

    /**
     * 启动当前的ServiceInstance
     */
    open fun start() {
        if (!isEnabled()) {
            if (logger.isDebugEnabled) {
                logger.debug("Discovery Lifecycle disabled. Not starting")
            }
            return
        }

        // 如果running状态为false, 才需要去进行启动, 避免重复注册
        if (!this.running.get()) {
            val context = this.applicationContext!!

            // 发布实例预注册事件, 即将进行服务的注册
            context.publishEvent(InstancePreRegisteredEvent(this, getRegistration()))

            // 注册服务
            register()

            // 发布实例已经注册的事件, 已经完成服务的注册
            context.publishEvent(InstanceRegisteredEvent(this, getConfiguration()))

            // CAS set state to true
            this.running.compareAndSet(false, true)
        }
    }

    /**
     * 获取要注册的服务(ServiceInstance)信息, 交给子类去完成实现
     *
     * @return 要进行注册的服务信息
     */
    protected abstract fun getRegistration(): R

    /**
     * 获取服务的注册的相关配置信息
     *
     * @return 服务的注册的相关配置信息
     */
    protected abstract fun getConfiguration(): Any

    /**
     * 当前ServiceInstance是否要去进行启用
     *
     * @return enabled?
     */
    protected open fun isEnabled(): Boolean = true

    /**
     * 停止当前ServiceInstance
     */
    open fun stop() {
        if (this.running.compareAndSet(true, false)) {
            deregister()
            this.registry.close()
        }
    }

    /**
     * 注册一个实例到ServiceInstance的ServiceRegistry当中
     */
    protected open fun register() {
        this.registry.register(getRegistration())
    }

    /**
     * 从ServiceRegistry当中取消注册一个ServiceInstance
     */
    protected open fun deregister() {
        this.registry.deregister(getRegistration())
    }
}