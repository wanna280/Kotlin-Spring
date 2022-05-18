package com.wanna.cloud.context.config.annotation

import com.wanna.framework.context.annotation.Scope

/**
 * 自定义的Scope，RefreshScope，标识这个Bean被划分在RefreshScope内
 *
 * @see com.wanna.cloud.context.scope.refresh.RefreshScope
 */
@Scope("refresh")
annotation class RefreshScope()
