package com.wanna.framework.context.event

import com.wanna.framework.context.ApplicationContext

/**
 * 由ApplicationContext发布的事件的基础类(source为ApplicationContext)
 *
 * @param applicationContext ApplicationContext
 *
 * @see ContextRefreshedEvent
 * @see ContextClosedEvent
 */
abstract class ApplicationContextEvent(val applicationContext: ApplicationContext) : ApplicationEvent(applicationContext)