package com.wanna.framework.context.annotation

import com.wanna.framework.core.annotation.AliasFor

/**
 * 自定义Bean的作用域(singleton/prototype/...)
 *
 * @param value scopeName
 * @param scopeName scopeName
 */
annotation class Scope(
    @get:AliasFor("scopeName")
    val value: String = "",
    @get:AliasFor("value")
    val scopeName: String = "",
    val proxyMode: ScopedProxyMode = ScopedProxyMode.DEFAULT
)
