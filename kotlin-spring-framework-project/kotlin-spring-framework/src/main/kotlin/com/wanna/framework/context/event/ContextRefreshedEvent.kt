package com.wanna.framework.context.event

/**
 * 这是一个容器刷新完成的事件, 当容器完成刷新时, 会自动发布这个事件
 *
 * @param source ApplicationContext
 */
open class ContextRefreshedEvent(source: com.wanna.framework.context.ApplicationContext) : ApplicationContextEvent(source)