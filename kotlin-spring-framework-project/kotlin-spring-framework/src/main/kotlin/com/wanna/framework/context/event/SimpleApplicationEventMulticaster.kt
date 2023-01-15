package com.wanna.framework.context.event

import com.wanna.framework.core.ResolvableType
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ErrorHandler
import java.util.concurrent.Executor

/**
 * 提供[ApplicationListener]的事件多拨器的简单实现
 *
 * @see ApplicationEventMulticaster
 */
open class SimpleApplicationEventMulticaster : AbstractApplicationEventMulticaster() {

    /**
     * 异步回调[ApplicationListener]监听器的线程池
     */
    @Nullable
    private var executor: Executor? = null

    /**
     * 处理回调[ApplicationListener]时发生异常的处理器
     */
    @Nullable
    private var errorHandler: ErrorHandler? = null

    /**
     * 设置回调监听器的线程池
     *
     * @param executor Executor
     */
    open fun setExecutor(executor: Executor) {
        this.executor = executor
    }

    /**
     * 设置处理异常的ErrorHandler
     *
     * @param errorHandler ErrorHandler
     */
    open fun setErrorHandler(errorHandler: ErrorHandler) {
        this.errorHandler = errorHandler
    }

    /**
     * 派发一个[ApplicationEvent]事件
     *
     * @param event 需要去进行派发的ApplicationEvent
     */
    override fun multicastEvent(event: ApplicationEvent) {
        multicastEvent(event, event::class.java)
    }

    /**
     * 发布事件, 可以执行事件的类型; 如果type为空, 那么默认情况下会采用event.class作为type
     *
     * @param event 需要去进行发布的事件
     * @param type 事件类型(可以为null)
     */
    override fun multicastEvent(event: ApplicationEvent, @Nullable type: Class<out ApplicationEvent>?) {
        val applicationEventClass = type ?: event::class.java
        val eventType = ResolvableType.forClass(applicationEventClass)
        multicastEvent(event, eventType)
    }


    /**
     * 发布事件, 可以执行事件的类型; 如果eventType为空, 那么默认情况下会采用event.class作为eventType
     *
     * @param event 需要去进行发布的事件
     * @param eventType 事件类型(ResolvableType, 可以为null)
     */
    override fun multicastEvent(event: ApplicationEvent, @Nullable eventType: ResolvableType?) {
        // 如果eventType为null, 那么将会采用event的类型作为事件类型
        val eventResolvableType = eventType ?: ResolvableType.forClass(event::class.java)
        // 根据事件类型, 去获取到所有的匹配的ApplicationListener
        val applicationListeners = getApplicationListeners<ApplicationEvent>(event, eventResolvableType)
        applicationListeners.forEach { invokeListener(it, event) }
    }

    /**
     * 执行监听器, 如果指定了errorHandler的话, 那么使用errorHandler去进行处理, 不然直接执行即可
     *
     * @param event event
     * @param applicationListener ApplicationListener
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
     * 执行监听器, 如果指定了Executor, 那么将会交给Executor去进行执行; 不然直接去进行回调监听器即可
     */
    protected open fun <E : ApplicationEvent> doInvokeListener(applicationListener: ApplicationListener<E>, event: E) {
        if (executor != null) {
            executor!!.execute { applicationListener.onApplicationEvent(event) }
        } else {
            applicationListener.onApplicationEvent(event)
        }
    }
}