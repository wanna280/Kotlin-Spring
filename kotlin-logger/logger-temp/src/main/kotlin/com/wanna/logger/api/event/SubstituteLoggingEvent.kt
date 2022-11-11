package com.wanna.logger.api.event

/**
 * 这是针对于Logger的Event的一个具体实现
 */
open class SubstituteLoggingEvent(_level: LoggingLevel, _msg: Any?, _loggerName: String) : LoggingEvent {
    private var level: LoggingLevel = _level
    private var msg: Any? = _msg
    private var loggerName: String = _loggerName
    private var timestamp: Long = System.currentTimeMillis()
    private var threadName: String = Thread.currentThread().name
    private var threadId: Long = Thread.currentThread().id

    private var throwable: Throwable? = null

    override fun toString(): String {
        return "$loggerName $timestamp $threadName $threadId ${level.name} $msg\n"
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

    override fun getLevel(): LoggingLevel {
        return level
    }
}