package com.wanna.framework.context.stereotype

import org.springframework.core.annotation.AliasFor

@Target(AnnotationTarget.CLASS)
@Component
annotation class Repository(
    @get:com.wanna.framework.core.annotation.AliasFor(value = "value", annotation = Component::class)
    @get:AliasFor(value = "value", annotation = Component::class)
    val value: String = ""
)
