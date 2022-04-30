package com.wanna.boot

import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 这是一个SpringApplication的运行的Listener的注册中心，维护了ApplicationListener列表，它负责在特定的时期去回调所有的监听器完成处理
 *
 * @see SpringApplicationRunListener
 */
open class SpringApplicationRunListeners(
    private val listeners: Collection<SpringApplicationRunListener>, args: Array<String>
) {
    /**
     * SpringApplication正在启动的过程当中
     */
    open fun starting(bootstrapContext: ConfigurableBootstrapContext) {
        doWithListeners("starting") { it.starting(bootstrapContext) }
    }

    /**
     * SpringApplication的环境已经准备好，可以对环境去进行后置处理
     */
    open fun environmentPrepared(
        bootstrapContext: ConfigurableBootstrapContext, configurableEnvironment: ConfigurableEnvironment
    ) {
        doWithListeners("environmentPrepared") { it.environmentPrepared(bootstrapContext, configurableEnvironment) }
    }

    /**
     * SpringApplication的ApplicationContext已经准备好
     */
    open fun contextPrepared(context: ConfigurableApplicationContext) {
        doWithListeners("contextPrepared") { it.contextPrepared(context) }
    }

    /**
     * SpringApplication的ApplicationContext已经刷新完成
     */
    open fun contextLoaded(context: ConfigurableApplicationContext) {
        doWithListeners("contextLoaded") { it.contextLoaded(context) }
    }

    /**
     * SpringApplication已经启动完成
     */
    open fun started(context: ConfigurableApplicationContext) {
        doWithListeners("started") { it.started(context) }
    }

    /**
     * SpringApplication正处于运行当中
     */
    open fun running(context: ConfigurableApplicationContext) {
        doWithListeners("running") { it.running(context) }
    }

    /**
     * SpringApplication启动失败
     */
    open fun failed(context: ConfigurableApplicationContext?, ex: Throwable) {
        doWithListeners("failed") { it.failed(context, ex) }
    }

    /**
     * 遍历所有的SpringApplicationRunListener，去完成监听器的回调
     */
    private fun doWithListeners(stepName: String, listenerAction: (SpringApplicationRunListener) -> Unit) {
        this.listeners.forEach(listenerAction::invoke)
    }
}