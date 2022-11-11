package com.wanna.boot.context.event

import com.wanna.boot.SpringApplication
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 这是一个SpringApplication启动失败的事件
 */
class ApplicationFailedEvent(
    val context: ConfigurableApplicationContext?,
    application: SpringApplication,
    args: Array<String>,
    val ex: Throwable
) : SpringApplicationEvent(application, args)