package com.wanna.framework.context.stereotype

import org.springframework.core.annotation.AliasFor

/**
 * 标识这是Spring容器中的Bean
 *
 * @param value beanName
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class Component(
    val value: String = ""
)