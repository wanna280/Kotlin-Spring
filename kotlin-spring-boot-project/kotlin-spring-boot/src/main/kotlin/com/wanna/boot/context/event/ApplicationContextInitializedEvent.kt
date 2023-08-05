package com.wanna.boot.context.event

import com.wanna.boot.SpringApplication
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 这是一个ApplicationContext已经初始化完成的事件(创建完ApplicationContext对象, 并执行完所有的ApplicationContextInitializer)
 */
open class ApplicationContextInitializedEvent(
    val context: ConfigurableApplicationContext,
    application: SpringApplication,
    args: Array<String>
) : SpringApplicationEvent(application, args)