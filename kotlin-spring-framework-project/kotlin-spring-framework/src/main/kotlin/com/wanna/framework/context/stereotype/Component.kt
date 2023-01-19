package com.wanna.framework.context.stereotype

/**
 * 标识这是一个Spring Bean
 *
 * @param value beanName
 * @see Service
 * @see Controller
 * @see Repository
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class Component(
    val value: String = ""
)