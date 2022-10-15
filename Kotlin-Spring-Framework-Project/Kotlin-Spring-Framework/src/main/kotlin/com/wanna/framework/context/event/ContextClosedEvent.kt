package com.wanna.framework.context.event

/**
 * ApplicationContext已经关闭的事件，用于通知所有处理这个事件的监听器，去完成收尾工作
 *
 * @param source ApplicationContext
 */
open class ContextClosedEvent(source: com.wanna.framework.context.ApplicationContext) : ApplicationContextEvent(source)