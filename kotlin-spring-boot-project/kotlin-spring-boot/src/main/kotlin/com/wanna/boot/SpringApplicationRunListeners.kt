package com.wanna.boot

import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.core.metrics.StartupStep
import com.wanna.common.logging.Logger
import java.util.function.Consumer

/**
 * 这是一个SpringApplication的运行的Listener的注册中心, 维护了ApplicationListener列表, 它负责在特定的时期去回调所有的监听器完成处理
 *
 * @see SpringApplicationRunListener
 *
 * @param listeners SpringApplicationRunListener列表
 * @param applicationStartup ApplicationStartup
 */
open class SpringApplicationRunListeners(
    private val listeners: Collection<SpringApplicationRunListener>,
    private val applicationStartup: ApplicationStartup,
    private val logger: Logger
) {
    /**
     * SpringApplication正在启动的过程当中
     *
     * @param bootstrapContext BootstrapContext
     * @param mainApplicationClass MainApplicationClass
     */
    open fun starting(bootstrapContext: ConfigurableBootstrapContext, mainApplicationClass: Class<*>?) {
        doWithListeners("starting", { it.starting(bootstrapContext) }) {
            if (mainApplicationClass != null) {
                it.tag("mainApplicationClass", mainApplicationClass.name)
            }
        }
    }

    /**
     * SpringApplication的环境已经准备好, 可以对环境去进行后置处理
     *
     * @param bootstrapContext BootstrapContext
     * @param configurableEnvironment Environment
     */
    open fun environmentPrepared(
        bootstrapContext: ConfigurableBootstrapContext, configurableEnvironment: ConfigurableEnvironment
    ) {
        doWithListeners("environmentPrepared") { it.environmentPrepared(bootstrapContext, configurableEnvironment) }
    }

    /**
     * SpringApplication的ApplicationContext已经准备好
     *
     * @param context ApplicationContext
     */
    open fun contextPrepared(context: ConfigurableApplicationContext) {
        doWithListeners("contextPrepared") { it.contextPrepared(context) }
    }

    /**
     * SpringApplication的ApplicationContext已经刷新完成
     *
     * @param context ApplicationContext
     */
    open fun contextLoaded(context: ConfigurableApplicationContext) {
        doWithListeners("contextLoaded") { it.contextLoaded(context) }
    }

    /**
     * SpringApplication已经启动完成
     *
     * @param context ApplicationContext
     */
    open fun started(context: ConfigurableApplicationContext) {
        doWithListeners("started") { it.started(context) }
    }

    /**
     * SpringApplication正处于运行当中
     *
     * @param context ApplicationContext
     */
    open fun running(context: ConfigurableApplicationContext) {
        doWithListeners("running") { it.running(context) }
    }

    /**
     * SpringApplication启动失败
     *
     * @param context ApplicationContext
     * @param ex 造成SpringApplication启动失败的异常原因
     */
    open fun failed(context: ConfigurableApplicationContext?, ex: Throwable) {
        doWithListeners("failed", { callFailedListener(it, context, ex) }) {
            it.tag("exception", ex::class.java.name)
            it.tag("message", ex.message ?: "")
        }
    }

    /**
     * 让处理失败的Listener去进行处理给定的异常
     *
     * @param listener SpringApplicationRunListener
     * @param context ApplicationContext
     * @param ex  造成SpringApplication启动失败的原因
     */
    private fun callFailedListener(
        listener: SpringApplicationRunListener,
        context: ConfigurableApplicationContext?,
        ex: Throwable
    ) {
        try {
            listener.failed(context, ex)
        } catch (ex: Throwable) {
            if (logger.isDebugEnabled) {
                logger.debug("处理异常失败", ex)
            } else {
                var message = ex.message
                message = message ?: "no error message"
                logger.warn("处理异常失败, 原因在于[$message]")
            }
        }
    }

    /**
     * 遍历所有的SpringApplicationRunListener, 去完成监听器的回调
     *
     * @param stepName stepName
     * @param listenerAction Listener要去执行的Callback
     */
    private fun doWithListeners(stepName: String, listenerAction: (SpringApplicationRunListener) -> Unit) =
        doWithListeners(stepName, listenerAction, null)

    /**
     * 1.遍历所有的SpringApplicationRunListener, 去完成监听器的回调;
     * 2.使用ApplicationStartup去打出来一个Step
     *
     * @param stepName stepName
     * @param listenerAction Listener要去执行的Callback
     * @param stepAction StepAction
     */
    private fun doWithListeners(
        stepName: String,
        listenerAction: (SpringApplicationRunListener) -> Unit,
        stepAction: Consumer<StartupStep>?
    ) {
        val step = applicationStartup.start(stepName)
        this.listeners.forEach(listenerAction::invoke)
        stepAction?.accept(step)
        step.end()
    }
}