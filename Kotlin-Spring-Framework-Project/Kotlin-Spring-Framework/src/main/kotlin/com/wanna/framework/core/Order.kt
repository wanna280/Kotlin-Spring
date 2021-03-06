package com.wanna.framework.core

/**
 * 具有排序功能的Order
 *
 * @see Ordered
 * @see PriorityOrdered
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
annotation class Order(
    val value: Int = Ordered.ORDER_LOWEST
)
