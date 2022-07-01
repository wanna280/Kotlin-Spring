package com.wanna.framework.context.stereotype

import org.springframework.core.annotation.AliasFor

@Component
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Controller(
    @get:AliasFor(value = "value", annotation = Component::class)
    val value: String = ""
)
