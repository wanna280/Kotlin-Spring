package com.wanna.framework.beans.annotations

import org.springframework.core.annotation.AliasFor

/**
 * 标识这是Spring当中的一个配置类
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Component
annotation class Configuration(
    @get:AliasFor(annotation = Component::class, value = "name")
    val name: String = "",

    @get:AliasFor(annotation = Component::class, value = "value")
    val value: String = "",
)
