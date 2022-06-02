package com.wanna.spring.shell.annotation

import com.wanna.framework.context.stereotype.Component
import org.springframework.core.annotation.AliasFor

/**
 * Shell场景下的Component
 */
@Component
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ShellComponent(
    @get:AliasFor("name", annotation = Component::class)
    val value: String = "",
    @get:AliasFor("value", annotation = Component::class)
    val name: String = ""
)
