package com.wanna.cloud.endpoint.event

import com.wanna.framework.context.event.ApplicationEvent

/**
 * RefreshEvent, 当环境发生刷新时会被自动发布
 *
 * @param source eventSource
 */
open class RefreshEvent(source: Any) : ApplicationEvent(source)