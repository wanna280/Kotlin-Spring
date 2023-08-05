package com.wanna.boot.context.event

import com.wanna.boot.ConfigurableBootstrapContext
import com.wanna.boot.SpringApplication

/**
 * 这是一个SpringApplication正在开始启动中的事件, 在开始启动时就会自动回调
 */
open class ApplicationStartingEvent(
    val bootstrapContext: ConfigurableBootstrapContext,
    application: SpringApplication,
    args: Array<String>
) : SpringApplicationEvent(application, args)