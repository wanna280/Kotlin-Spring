package com.wanna.framework.context.stereotype

/**
 * 标识这是Spring容器中的Bean
 *
 * @param value beanName
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class Component(
    val value: String = ""
)