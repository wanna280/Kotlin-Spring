package com.wanna.framework.context.event

import kotlin.reflect.KClass

/**
 * 监听对应类型的事件的发布的监听器, 当对应类型的事件触发时, 就会自动触发Callback
 *
 * @param value 需要监听的事件类型, 同classes属性
 * @param classes 需要监听的事件类型, 同value属性
 * @param id EventListenerId
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
annotation class EventListener(
    val value: Array<KClass<*>> = [],
    val classes: Array<KClass<*>> = [],
    val id: String = ""
)
