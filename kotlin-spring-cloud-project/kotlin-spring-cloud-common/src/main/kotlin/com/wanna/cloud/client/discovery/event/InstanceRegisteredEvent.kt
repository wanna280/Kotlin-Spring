package com.wanna.cloud.client.discovery.event

import com.wanna.framework.context.event.ApplicationEvent

/**
 * 实例注册完成事件, 在ServiceInstance注册在注册中心(ServiceRegistry)之后, 会自动触发这个事件
 *
 * @param source source
 */
open class InstanceRegisteredEvent(source: Any, config: Any) : ApplicationEvent(source)