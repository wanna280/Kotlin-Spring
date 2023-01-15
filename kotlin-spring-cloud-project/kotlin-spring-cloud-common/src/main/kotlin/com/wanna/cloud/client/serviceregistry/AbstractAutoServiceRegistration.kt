package com.wanna.cloud.client.serviceregistry

import com.wanna.cloud.client.discovery.event.InstancePreRegisteredEvent
import com.wanna.cloud.client.discovery.event.InstanceRegisteredEvent
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.core.environment.Environment
import com.wanna.boot.web.server.WebServerInitializedEvent
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 为服务的自动注册提供抽象的模板方法实现, 它组合了ServiceRegistry, 去完成服务的**自动注册**
 *
 * @see start
 * @see stop
 */
abstract class AbstractAutoServiceRegistration<R : Registration>(
    private val registry: ServiceRegistry<R>,
    private val properties: AutoServiceRegistrationProperties
) : AutoServiceRegistration, ApplicationContextAware, ApplicationListener<WebServerInitializedEvent> {

    private val running: AtomicBoolean = AtomicBoolean(false)

    private var applicationContext: ApplicationContext? = null

    private var environment: Environment? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        this.environment = applicationContext.getEnvironment()
    }

    protected open fun getApplicationContext(): ApplicationContext {
        return this.applicationContext!!
    }

    protected open fun getEnvironment(): Environment {
        return this.environment!!
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

    open fun start() {
        // 如果running状态为false, 才需要去进行启动
        if (!this.running.get()) {
            val context = this.applicationContext!!

            // 发布实例预注册事件
            context.publishEvent(InstancePreRegisteredEvent(this, getRegistration()))

            // 注册服务
            register()

            // 发布实例已经注册的事件
            context.publishEvent(InstanceRegisteredEvent(this, Any()))

            this.running.compareAndSet(false, true)
        }
    }

    /**
     * 获取要注册的服务(ServiceInstance), 交给子类去完成实现
     *
     * @return 要进行注册的服务
     */
    protected abstract fun getRegistration(): R

    open fun stop() {
        if (this.running.compareAndSet(true, false)) {
            deregister()
            this.registry.close()
        }
    }

    /**
     * 注册一个实例到ServiceInstance的Registry当中
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