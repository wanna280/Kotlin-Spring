package com.wanna.framework.context.annotation

import org.springframework.core.annotation.AliasFor

annotation class Scope(
    @get:com.wanna.framework.core.annotation.AliasFor("scopeName")
    @get:AliasFor("scopeName")
    val value: String = "",
    @get:com.wanna.framework.core.annotation.AliasFor("value")
    @get:AliasFor("value")
    val scopeName: String = "",
    val proxyMode: ScopedProxyMode = ScopedProxyMode.DEFAULT
)
