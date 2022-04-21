package com.wanna.logger.impl

import com.wanna.logger.api.Logger
import com.wanna.logger.api.event.SubstituteLoggingEvent
import com.wanna.logger.api.event.LoggingLevel
import com.wanna.logger.impl.appender.ConsoleLoggerAppender
import com.wanna.logger.impl.appender.LoggerAppender

/**
 * 这是一个自定义的Logger，针对于API规范中的Logger提供具体的实现
 */
open class LogcLogger : Logger {

    // loggerContext
    private var loggerContext: LoggerContext? = null

    // parent Logger
    private var parent: Logger? = null

    // LoggerName
    private var loggerName = this::class.java.name

    // 全局的Level
    private var globalLevel = LoggingLevel.DEBUG

    // Appender，使用输出流的方式，将日志输出到对应的位置，比如Console/LogFile
    private var appenderList = ArrayList<LoggerAppender>()

    fun addAppender(appender: LoggerAppender) {
        appenderList += appender
    }

    override fun info(msg: String) {
        filterAndLog(LoggingLevel.INFO, msg)
    }

    override fun debug(msg: String) {
        filterAndLog(LoggingLevel.DEBUG, msg)
    }

    override fun warn(msg: String) {
        filterAndLog(LoggingLevel.WARN, msg)
    }

    override fun error(msg: String) {
        filterAndLog(LoggingLevel.ERROR, msg)
    }

    override fun trace(msg: String) {
        filterAndLog(LoggingLevel.TRACE, msg)
    }

    private fun filterAndLog(level: LoggingLevel, msg: Any?) {
        var logger: LogcLogger? = this

        // 如果当前Logger没有Appender去进行日志的输出，但是parent Logger可以去完成日志的输出
        // 那么就交给parent去进行日志的输出
        while (logger != null) {
            if (logger.getAppenderList().isEmpty()) {
                logger = logger.getParent()
                continue
            }
            if (level.level >= globalLevel.level) {
                val event = SubstituteLoggingEvent(level, msg, loggerName)

                // 这里应该使用logger.getAppenderList，而不是this.appenderList
                logger.getAppenderList().forEach { appender ->
                    appender.append(event)
                }
            }
            break
        }
    }

    override fun isDebugEnabled(): Boolean {
        return globalLevel.level <= LoggingLevel.DEBUG.level
    }

    override fun isTraceEnabled(): Boolean {
        return globalLevel.level <= LoggingLevel.TRACE.level
    }

    override fun getLoggerName(): String {
        return loggerName
    }

    override fun getParent(): LogcLogger? {
        return parent as LogcLogger?
    }

    override fun setParent(logger: Logger) {
        this.parent = logger
    }

    fun getAppenderList(): List<LoggerAppender> {
        return appenderList
    }

    override fun setLoggerName(name: String) {
        this.loggerName = name
    }

    fun setLoggerContext(context: LoggerContext) {
        this.loggerContext = context
    }
}