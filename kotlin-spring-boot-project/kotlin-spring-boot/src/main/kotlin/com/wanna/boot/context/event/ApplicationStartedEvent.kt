package com.wanna.boot.context.event

import com.wanna.boot.SpringApplication
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 这是一个SpringApplication已经启动完成的事件, ApplicationContext已经完成刷新
 */
open class ApplicationStartedEvent(
    application: SpringApplication,
    val context: ConfigurableApplicationContext,
    args: Array<String>
) : SpringApplicationEvent(application, args)
