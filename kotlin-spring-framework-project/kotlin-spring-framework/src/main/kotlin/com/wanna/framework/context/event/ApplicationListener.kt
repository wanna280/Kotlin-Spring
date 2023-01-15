package com.wanna.framework.context.event

/**
 * 这是一个应用程序的监听器, 可以在事件触发时, 自动回调
 *
 * @param E 支持去进行处理的事件类型
 * @see ApplicationEvent
 */
fun interface ApplicationListener<E : ApplicationEvent> {

    /**
     * 当事件触发时, 会自动回调的方法, 交给子类去进行实现
     *
     * @param event 要进行发布的事件对象, 不能为null
     */
    fun onApplicationEvent(event: E)
}