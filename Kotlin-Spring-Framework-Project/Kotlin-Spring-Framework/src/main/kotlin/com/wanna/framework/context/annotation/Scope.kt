package com.wanna.framework.context.annotation

import org.springframework.core.annotation.AliasFor

annotation class Scope(
    @get:AliasFor("scopeName")
    val value: String = "",
    @get:AliasFor("value")
    val scopeName: String = "",
    val proxyMode: ScopedProxyMode = ScopedProxyMode.DEFAULT
)
