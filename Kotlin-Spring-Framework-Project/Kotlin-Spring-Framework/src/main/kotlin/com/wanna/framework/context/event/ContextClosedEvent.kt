package com.wanna.framework.context.event

import com.wanna.framework.context.ApplicationContext

/**
 * ApplicationContext已经关闭的事件，用于通知所有处理这个事件的监听器，去完成收尾工作
 *
 * @param source 发布事件的事件源ApplicationContext
 */
open class ContextClosedEvent(source: ApplicationContext) : ApplicationContextEvent(source)