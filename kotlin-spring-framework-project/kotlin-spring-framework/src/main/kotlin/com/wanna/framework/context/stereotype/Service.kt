package com.wanna.framework.context.stereotype

import org.springframework.core.annotation.AliasFor

@Component
annotation class Service(
    @get:AliasFor(value = "value", annotation = Component::class)
    val value: String = ""
)
