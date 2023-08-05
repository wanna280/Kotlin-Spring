package com.wanna.framework.core

/**
 * 具有排序功能的Order
 *
 * @see Ordered
 * @see PriorityOrdered
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
annotation class Order(
    val value: Int = Ordered.ORDER_LOWEST
)
