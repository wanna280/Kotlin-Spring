package com.wanna.framework.context.stereotype

import org.springframework.core.annotation.AliasFor

/**
 * 标识这是Spring容器中的Bean
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class Component(
    /**
     * Kotlin当中要使用Java中在注解的属性上标注注解的方式，可以使用getter配合
     */
    @get:AliasFor("name")
    val value: String = "",

    @get:AliasFor("value")
    val name: String = ""
)