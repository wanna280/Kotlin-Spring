package com.wanna.framework.context.event

import com.wanna.framework.core.util.ErrorHandler
import java.util.concurrent.Executor

/**
 * 这是一个提供ApplicationListener的多拨器的简单实现
 */
open class SimpleApplicationEventMulticaster : AbstractApplicationEventMulticaster() {

    private var executor: Executor? = null  // 回调监听器的线程池

    private var errorHandler: ErrorHandler? = null  // 异常处理器

    open fun setExecutor(executor: Executor) {
        this.executor = executor
    }

    open fun setErrorHandler(errorHandler: ErrorHandler) {
        this.errorHandler = errorHandler
    }

    override fun multicastEvent(event: ApplicationEvent) {
        multicastEvent(event, event::class.java)
    }

    override fun multicastEvent(event: ApplicationEvent, type: Class<out ApplicationEvent>?) {
        val eventType = type ?: event::class.java
        // 根据事件类型，去获取到所有的匹配的ApplicationListener
        val applicationListeners = getApplicationListeners(event, eventType)
        applicationListeners.forEach { invokeListener(it, event) }
    }

    /**
     * 执行监听器，如果指定了errorHandler的话，那么使用errorHandler去进行处理，不然直接执行即可
     */
    protected open fun invokeListener(applicationListener: ApplicationListener<*>, event: ApplicationEvent) {
        if (errorHandler != null) {
            try {
                doInvokeListener(applicationListener, event)
            } catch (ex: Throwable) {
                errorHandler!!.handleError(ex)
            }
        } else {
            doInvokeListener(applicationListener, event)
        }
    }

    /**
     * 执行监听器，如果指定了Executor，那么将会交给Executor去进行执行；不然直接去进行回调监听器即可
     */
    protected open fun doInvokeListener(applicationListener: ApplicationListener<*>, event: ApplicationEvent) {
        if (executor != null) {
            executor!!.execute { applicationListener.onApplicationEvent(event) }
        } else {
            applicationListener.onApplicationEvent(event)
        }
    }
}