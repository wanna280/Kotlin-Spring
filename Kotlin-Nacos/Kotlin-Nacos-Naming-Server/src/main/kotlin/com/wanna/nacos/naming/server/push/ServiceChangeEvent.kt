package com.wanna.nacos.naming.server.push

import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.nacos.naming.server.core.NamingService

/**
 * Nacos的NamingService发生改变的事件
 *
 * @param source source(发布事件的来源)
 * @param service 发生改变的NamingService
 */
class ServiceChangeEvent(source: Any, val service: NamingService) : ApplicationEvent(source)