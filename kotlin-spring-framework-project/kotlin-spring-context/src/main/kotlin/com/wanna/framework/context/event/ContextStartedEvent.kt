package com.wanna.framework.context.event

import com.wanna.framework.context.ApplicationContext

/**
 * [ApplicationContext]已经启动的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/7
 *
 * @param source 发布事件的ApplicationContext事件源
 */
open class ContextStartedEvent(source: ApplicationContext) : ApplicationEvent(source)