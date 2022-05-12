package com.wanna.logger.impl.event

import com.wanna.logger.impl.AbstractLoggerContext
import com.wanna.logger.impl.LogcLogger


/**
 * 这是针对于Logger的LoggingEvent的一个具体实现
 *
 * @param loggerQualifierName logger的全类名，可以利用它去寻找callerInfo信息
 * @param level level
 * @param msg logMessage
 * @param logger logger
 * @param throwable 异常信息
 */
open class LoggingEvent(
    private val loggerQualifierName: String,
    private val level: Level,
    private val msg: Any?,
    private val logger: LogcLogger,
    private val throwable: Throwable?
) : ILoggingEvent {
    private val loggerName: String = logger.getLoggerName()
    private val loggerContext = logger.getLoggerContext()
    private val timestamp: Long = System.currentTimeMillis()
    private val threadName: String = Thread.currentThread().name
    private val threadId: Long = Thread.currentThread().id

    constructor(
        _loggerQualifierName: String, _level: Level, _msg: Any?, logger: LogcLogger
    ) : this(_loggerQualifierName, _level, _msg, logger, null)

    /**
     * 获取线程id
     */
    override fun getThreadId(): Long {
        return threadId
    }

    /**
     * 获取线程名
     */
    override fun getThreadName(): String {
        return threadName
    }

    /**
     * 获取要输出的日志消息
     */
    override fun getMessage(): String? {
        return msg?.toString()
    }

    /**
     * 获取时间戳
     */
    override fun getTimestamp(): Long {
        return timestamp
    }

    /**
     * 获取Throwable异常信息
     */
    override fun getThrowable(): Throwable? {
        return throwable
    }

    /**
     * 获取LoggerName
     */
    override fun getLoggerName(): String {
        return loggerName
    }

    /**
     * 获取LoggingEvent的LoggingLevel
     */
    override fun getLevel(): Level {
        return level
    }

    /**
     * 获取Logger的全类名
     */
    open fun getLoggerQualifierName(): String {
        return this.loggerQualifierName
    }

    /**
     * 获取LoggerContext，有可能为null
     */
    open fun getLoggerContext() = this.loggerContext

    override fun toString(): String {
        return "LoggingEvent[$loggerName $timestamp $threadName $threadId ${level.name} $msg $throwable]"
    }
}