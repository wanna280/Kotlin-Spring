package com.wanna.framework.core

/**
 * 具有排序功能的Order
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
annotation class Order(
    val value: Int
)
