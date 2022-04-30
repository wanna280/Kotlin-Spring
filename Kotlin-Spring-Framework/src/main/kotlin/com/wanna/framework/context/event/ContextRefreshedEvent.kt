package com.wanna.framework.context.event

import com.wanna.framework.context.ApplicationContext

/**
 * 这是一个容器刷新完成的事件，当容器完成刷新时，会自动发布这个事件
 */
class ContextRefreshedEvent(applicationContext: ApplicationContext) : ApplicationEvent(applicationContext)