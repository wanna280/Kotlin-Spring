package com.wanna.framework.context.event

import java.util.EventObject

/**
 * 这是一个要用于进行发布的事件, 可以被ApplicationEventMulticaster或者ApplicationEventPublisher用于去进行发布
 *
 * @see ApplicationEventMulticaster
 * @see ApplicationEventPublisher
 *
 * @param source 事件发布源, 事件从哪个对象当中发布？
 */
open class ApplicationEvent(source: Any?) : EventObject(source)