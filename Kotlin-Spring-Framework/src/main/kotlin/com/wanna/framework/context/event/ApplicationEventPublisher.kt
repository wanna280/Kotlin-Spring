package com.wanna.framework.context.event

/**
 * 这是一个事件发布器，可以利用这个组件完成事件的发布工作
 */
interface ApplicationEventPublisher {
    /**
     * 利用任意类型去发布一个事件，Any对应Java当中的Object
     */
    fun publishEvent(event: Any)

    /**
     * 发布一个ApplicationEvent类型的事件
     */
    fun publishEvent(event: ApplicationEvent) = publishEvent(event as Any)
}