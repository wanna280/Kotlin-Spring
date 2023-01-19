package com.wanna.boot.context.event

import com.wanna.boot.SpringApplication
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 这是一个SpringApplication已经处于运行中的状态的事件(容器已经完成刷新, 处于运行当中的状态)
 *
 * @param context ApplicationContext
 * @param application SpringApplication
 * @param args 命令行参数列表
 *
 * @see SpringApplicationEvent
 */
open class ApplicationReadyEvent(
    val context: ConfigurableApplicationContext,
    application: SpringApplication,
    args: Array<String>
) : SpringApplicationEvent(application, args)