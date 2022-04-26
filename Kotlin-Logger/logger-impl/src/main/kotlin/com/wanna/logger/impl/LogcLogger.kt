package com.wanna.logger.impl

import com.wanna.logger.api.Logger
import com.wanna.logger.impl.appender.LoggerAppender
import com.wanna.logger.impl.event.Level
import com.wanna.logger.impl.event.LoggingEvent
import com.wanna.logger.impl.filter.FilterReply
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 这是一个自定义的Logger，针对于API规范中的Logger提供具体的实现
 * 这个组件必须遵循API规范，因为API规范当中，getLogger获取到的就是这个Logger对象
 */
open class LogcLogger() : Logger {

    companion object {
        const val ROOT_LOGGER_NAME = "ROOT"
    }

    constructor(name: String) : this() {
        this.loggerName = name
    }

    // loggerContext
    private var loggerContext: LoggerContext? = null

    // parent Logger
    private var parent: LogcLogger? = null

    // children Logger，为了保证并发安全，采用COW的ArrayList
    private val children = CopyOnWriteArrayList<LogcLogger>()

    // LoggerName
    private var loggerName = this::class.java.name

    // Logger自身的Level
    var level = Level.INFO

    // Appender，使用输出流的方式，将日志输出到对应的位置，比如Console/LogFile
    private var appenderList = CopyOnWriteArrayList<LoggerAppender>()

    fun addAppender(appender: LoggerAppender) {
        appenderList += appender
    }

    override fun info(msg: String) {
        filterAndLog(Level.INFO, msg)
    }

    override fun debug(msg: String) {
        filterAndLog(Level.DEBUG, msg)
    }

    override fun warn(msg: String) {
        filterAndLog(Level.WARN, msg)
    }

    override fun error(msg: String) {
        filterAndLog(Level.ERROR, msg)
    }

    override fun trace(msg: String) {
        filterAndLog(Level.TRACE, msg)
    }

    private fun filterAndLog(level: Level, msg: Any?) {
        // 让LoggerFilter去决策本次日志，是否要进行输出
        val reply = loggerContext!!.filterList.getFilterChainDecisionReply(this, level, msg, emptyArray(), null)

        // 如果最终的结果是DENY的话，那么需要拒绝，不进行本次日志的输出，return
        if (reply == FilterReply.DENY) {
            return
        }

        // 构建LoggingEvent并使用Appender其完成Append
        buildLoggingEventAndAppend(level, msg)
    }

    /**
     * 构建LoggingEvent并使用Appender其完成Append
     */
    private fun buildLoggingEventAndAppend(level: Level, msg: Any?) {
        var logger: LogcLogger? = this

        // 如果当前Logger没有Appender去进行日志的输出，但是parent Logger可以去完成日志的输出
        // 那么就交给parent去进行日志的输出
        while (logger != null) {
            if (logger.getAppenderList().isEmpty()) {
                logger = logger.getParent()
                continue
            }
            if (logger.level.level <= level.level) {
                val event = LoggingEvent(level, msg, loggerName)

                // 这里应该使用logger.getAppenderList，而不是this.appenderList
                logger.getAppenderList().forEach { appender ->
                    appender.append(event)
                }
            }
            break
        }
    }

    /**
     * 通过name去获取childLogger
     */
    fun getChildByName(name: String): LogcLogger? {
        for (child in children) {
            if (child.getLoggerName() == name) {
                return child
            }
        }
        return null
    }

    /**
     * 根据name去创建childLogger，并加入到当前的Logger的children列表当中去
     */
    fun createChildByName(name: String): LogcLogger {
        // 创建一个Logger，并设置parent为this
        val logcLogger = LogcLogger(name)
        logcLogger.setParent(this)
        logcLogger.setLoggerContext(this.loggerContext!!)
        logcLogger.level = this.level

        children += logcLogger
        return logcLogger
    }

    override fun isDebugEnabled(): Boolean {
        return level.level <= Level.DEBUG.level
    }

    override fun isTraceEnabled(): Boolean {
        return level.level <= Level.TRACE.level
    }

    override fun getLoggerName(): String {
        return loggerName
    }

    override fun getParent(): LogcLogger? {
        return parent
    }

    override fun setParent(logger: Logger) {
        this.parent = logger as LogcLogger
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

    override fun toString(): String {
        return this.loggerName
    }
}