package com.wanna.framework.context.stereotype

import com.wanna.framework.core.annotation.AliasFor

/**
 * 标识这是一个Controller层的Spring Bean
 *
 * @param value beanName
 *
 * @see Component
 */
@Component
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Controller(
    @get:AliasFor(value = "value", annotation = Component::class)
    val value: String = ""
)
