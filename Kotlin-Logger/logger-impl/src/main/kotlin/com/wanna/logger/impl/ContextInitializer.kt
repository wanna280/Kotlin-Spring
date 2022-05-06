package com.wanna.logger.impl


import com.wanna.logger.impl.appender.ConsoleLoggerAppender
import com.wanna.logger.impl.appender.FileLoggerAppender
import com.wanna.logger.impl.event.Level

/**
 * 这是一个Logger的上下文初始化器，去对LoggerContext去完成初始化，在这里可以去完成配置文件的加载，从而去实现对于整个Logger的配置
 */
open class ContextInitializer<T : LogcLogger>(private val abstractLoggerContext: AbstractLoggerContext<T>) {

    /**
     * 需要在这里去完成自动配置，完成配置文件的加载，并实现对LoggerContext的配置工作
     */
    open fun autoConfig() {
        abstractLoggerContext.root.setLevel(Level.DEBUG)
        abstractLoggerContext.root.addAppender(FileLoggerAppender())
        abstractLoggerContext.root.addAppender(ConsoleLoggerAppender())
    }

    open fun getLoggerContext(): AbstractLoggerContext<T> = this.abstractLoggerContext
}