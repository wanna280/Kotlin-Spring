package com.wanna.framework.context.event

import com.wanna.framework.core.ResolvableType
import com.wanna.framework.lang.Nullable

/**
 * 这是一个ApplicationEvent的多拨器，它可以维护监听器列表，并完成事件的发布
 *
 * @see ApplicationEventPublisher
 */
interface ApplicationEventMulticaster {

    /**
     * 添加ApplicationListener
     *
     * @param listener 需要添加的ApplicationListener对象
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
     *
     * @param listener 需要移除的ApplicationListener对象
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
     *
     * @param event 需要去进行发布的事件
     */
    fun multicastEvent(event: ApplicationEvent)

    /**
     * 发布事件，可以执行事件的类型; 如果type为空，那么默认情况下会采用event.class作为type
     *
     * @param event 需要去进行发布的事件
     * @param type 事件类型(可以为null)
     */
    fun multicastEvent(event: ApplicationEvent, @Nullable type: Class<out ApplicationEvent>?)

    /**
     * 发布事件，可以执行事件的类型; 如果eventType为空，那么默认情况下会采用event.class作为eventType
     *
     * @param event 需要去进行发布的事件
     * @param eventType 事件类型(ResolvableType, 可以为null)
     */
    fun multicastEvent(event: ApplicationEvent, @Nullable eventType: ResolvableType?)

}