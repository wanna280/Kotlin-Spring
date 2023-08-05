package com.wanna.nacos.naming.server.push

import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.nacos.naming.server.core.NamingService

/**
 * Nacos的NamingService发生改变的事件, 当该实例发生变化时, 就会利用ApplicationContext去自动推送这个事件
 *
 * @param source source(发布事件的来源)
 * @param service 发生改变的NamingService
 */
open class ServiceChangeEvent(source: Any, val service: NamingService) : ApplicationEvent(source)