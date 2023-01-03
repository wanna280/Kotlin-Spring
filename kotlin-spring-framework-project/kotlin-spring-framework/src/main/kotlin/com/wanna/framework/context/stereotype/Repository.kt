package com.wanna.framework.context.stereotype

import com.wanna.framework.core.annotation.AliasFor

@Target(AnnotationTarget.CLASS)
@Component
annotation class Repository(
    @get:AliasFor(value = "value", annotation = Component::class)
    val value: String = ""
)
