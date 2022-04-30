package com.wanna.framework.context.event

/**
 * 这是一个ApplicationEvent的多拨器，它可以维护监听器列表，并完成事件的发布
 *
 * @see ApplicationEventPublisher
 */
interface ApplicationEventMulticaster {

    /**
     * 添加ApplicationListener
     */
    fun addApplicationListener(listener: ApplicationListener<*>)

    /**
     * 添加ApplicationListenerBean
     *
     * @param listenerBeanName ApplicationListener的beanName
     */
    fun addApplicationListenerBean(listenerBeanName: String)

    /**
     * 移除ApplicationListener
     */
    fun removeApplicationListener(listener: ApplicationListener<*>)

    /**
     * 移除ApplicationListenerBean
     *
     * @param listenerBeanName ApplicationListener的beanName
     */
    fun removeApplicationListenerBean(listenerBeanName: String)

    /**
     * 移除所有的ApplicationListener
     */
    fun removeAllApplicationListeners()

    /**
     * 发布事件
     */
    fun multicastEvent(event: ApplicationEvent)

    /**
     * 发布事件，可以执行事件的类型；如果type为空，那么默认情况下会采用event.class作为type
     */
    fun multicastEvent(event: ApplicationEvent, type: Class<out ApplicationEvent>?)

}