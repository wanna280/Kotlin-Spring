package com.wanna.framework.context.event

import com.wanna.framework.core.ResolvableType
import com.wanna.framework.util.ErrorHandler
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
        val applicationEventClass = type ?: event::class.java
        val eventType = ResolvableType.forClass(applicationEventClass)
        multicastEvent(event, eventType)
    }

    override fun multicastEvent(event: ApplicationEvent, eventType: ResolvableType?) {
        // 如果eventType为null，那么将会采用event的类型作为事件类型
        val eventResolvableType = eventType ?: ResolvableType.forClass(event::class.java)
        // 根据事件类型，去获取到所有的匹配的ApplicationListener
        val applicationListeners = getApplicationListeners<ApplicationEvent>(event, eventResolvableType)
        applicationListeners.forEach { invokeListener(it, event) }
    }

    /**
     * 执行监听器，如果指定了errorHandler的话，那么使用errorHandler去进行处理，不然直接执行即可
     */
    protected open fun <E : ApplicationEvent> invokeListener(applicationListener: ApplicationListener<E>, event: E) {
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
    protected open fun <E : ApplicationEvent> doInvokeListener(applicationListener: ApplicationListener<E>, event: E) {
        if (executor != null) {
            executor!!.execute { applicationListener.onApplicationEvent(event) }
        } else {
            applicationListener.onApplicationEvent(event)
        }
    }
}