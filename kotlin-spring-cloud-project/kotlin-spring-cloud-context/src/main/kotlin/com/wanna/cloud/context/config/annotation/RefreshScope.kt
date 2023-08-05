package com.wanna.cloud.context.config.annotation

import com.wanna.framework.context.annotation.Scope
import com.wanna.framework.context.annotation.ScopedProxyMode
import com.wanna.framework.core.annotation.AliasFor

/**
 * 自定义的Scope, RefreshScope, 标识这个Bean被划分在RefreshScope内
 *
 * @see com.wanna.cloud.context.scope.refresh.RefreshScope
 */
@Scope("refresh")
annotation class RefreshScope(
    @get:AliasFor(annotation = Scope::class, value = "proxyMode")
    val proxyMode: ScopedProxyMode = ScopedProxyMode.TARGET_CLASS
)
