package com.wanna.framework.context.event

import com.wanna.framework.context.ApplicationContext

/**
 * 这是一个事件发布器, 可以利用这个Publisher去完成Spring的ApplicationEvent事件的发布工作
 *
 * @see ApplicationEvent
 * @see ApplicationContext
 * @see ApplicationEventMulticaster
 */
interface ApplicationEventPublisher {
    /**
     * 利用任意类型去发布一个事件, Any对应Java当中的Object
     *
     * @param event 要去进行发布的事件对象
     */
    fun publishEvent(event: Any)

    /**
     * 发布一个ApplicationEvent类型的事件
     *
     * @param event 要去进行发布的ApplicationEvent
     */
    fun publishEvent(event: ApplicationEvent) = publishEvent(event as Any)
}