package com.wanna.boot.context.event

import com.wanna.boot.SpringApplication
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 这是一个SpringApplication已经准备好的事件, ApplicationContext完成创建并完成配置类的加载之后会被回调(还未刷新)
 */
open class ApplicationPreparedEvent(
    val context: ConfigurableApplicationContext,
    application: SpringApplication,
    args: Array<String>
) : SpringApplicationEvent(application, args)