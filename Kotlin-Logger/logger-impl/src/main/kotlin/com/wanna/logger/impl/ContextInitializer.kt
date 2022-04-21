package com.wanna.logger.impl

import com.wanna.logger.impl.appender.ConsoleLoggerAppender
import com.wanna.logger.impl.appender.FileLoggerAppender

/**
 * 这是一个Logger的上下文初始化器，去对LoggerContext去完成初始化
 * 在这里可以去完成配置文件的加载，从而去实现对于整个Logger的配置
 */
open class ContextInitializer(private val loggerContext: LoggerContext) {

    /**
     * 需要在这里去完成自动配置，完成配置文件的加载，并实现对LoggerContext的配置工作
     */
    fun autoConfig() {
        val logger = LogcLogger()
        logger.addAppender(ConsoleLoggerAppender())
        logger.addAppender(FileLoggerAppender())

        loggerContext.root = logger
    }
}