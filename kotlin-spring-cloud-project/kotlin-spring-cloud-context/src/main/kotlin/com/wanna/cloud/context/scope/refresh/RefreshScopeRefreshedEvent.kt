package com.wanna.cloud.context.scope.refresh

import com.wanna.framework.context.event.ApplicationEvent

/**
 * RefreshScope的刷新事件
 */
open class RefreshScopeRefreshedEvent(val name: String) : ApplicationEvent(name) {
    constructor() : this(DEFAULT_NAME)

    companion object {
        const val DEFAULT_NAME = "__refreshAll__"
    }
}