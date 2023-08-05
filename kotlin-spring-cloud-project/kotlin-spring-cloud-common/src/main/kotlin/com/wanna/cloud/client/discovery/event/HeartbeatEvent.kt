package com.wanna.cloud.client.discovery.event

import com.wanna.framework.context.event.ApplicationEvent

/**
 * 这是一个心跳事件, 在Client与Server之间发送心跳时, 会自动发布这个事件
 *
 * @param source source
 * @param state 心跳的状态信息
 */
open class HeartbeatEvent(source: Any, state: Any) : ApplicationEvent(source)