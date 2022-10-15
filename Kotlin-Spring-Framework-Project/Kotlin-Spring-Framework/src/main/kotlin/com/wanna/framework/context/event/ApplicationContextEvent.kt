package com.wanna.framework.context.event

/**
 * 由ApplicationContext发布的事件的基础类(source为ApplicationContext)
 *
 * @param applicationContext ApplicationContext
 *
 * @see ContextRefreshedEvent
 * @see ContextClosedEvent
 */
abstract class ApplicationContextEvent(val applicationContext: com.wanna.framework.context.ApplicationContext) : ApplicationEvent(applicationContext)