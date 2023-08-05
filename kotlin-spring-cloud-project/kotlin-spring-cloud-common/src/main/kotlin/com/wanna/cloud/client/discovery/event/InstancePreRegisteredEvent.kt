package com.wanna.cloud.client.discovery.event

import com.wanna.cloud.client.serviceregistry.Registration
import com.wanna.framework.context.event.ApplicationEvent

/**
 * 实例预注册实现, 在即将要去注册一个ServiceInstance到注册中心(ServiceRegistry)当中时, 会自动发布这个事件
 *
 * @param source source
 * @param registration 要进行注册的实例
 */
open class InstancePreRegisteredEvent(source: Any, val registration: Registration) : ApplicationEvent(source)