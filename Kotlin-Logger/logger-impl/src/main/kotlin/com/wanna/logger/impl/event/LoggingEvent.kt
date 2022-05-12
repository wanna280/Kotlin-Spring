package com.wanna.logger.impl.event


/**
 * 这是针对于Logger的LoggingEvent的一个具体实现
 *
 * @param loggerQualifierName logger的全类名，可以利用它去寻找callerInfo信息
 * @param level level
 * @param msg logMessage
 * @param loggerName loggerName
 * @param throwable 异常信息
 */
open class LoggingEvent(
    private val loggerQualifierName: String,
    private val level: Level,
    private val msg: Any?,
    private val loggerName: String,
    private val throwable: Throwable?
) : ILoggingEvent {
    private val timestamp: Long = System.currentTimeMillis()
    private val threadName: String = Thread.currentThread().name
    private val threadId: Long = Thread.currentThread().id

    constructor(
        _loggerQualifierName: String, _level: Level, _msg: Any?, _loggerName: String
    ) : this(_loggerQualifierName, _level, _msg, _loggerName, null)

    override fun toString(): String {
        return "LoggingEvent[$loggerName $timestamp $threadName $threadId ${level.name} $msg $throwable]"
    }

    override fun getThreadId(): Long {
        return threadId
    }

    override fun getThreadName(): String {
        return threadName
    }

    override fun getMessage(): String? {
        return msg?.toString()
    }

    override fun getTimestamp(): Long {
        return timestamp
    }

    override fun getThrowable(): Throwable? {
        return throwable
    }

    override fun getLoggerName(): String {
        return loggerName
    }

    override fun getLevel(): Level {
        return level
    }

    fun getLoggerQualifierName(): String {
        return this.loggerQualifierName
    }
}