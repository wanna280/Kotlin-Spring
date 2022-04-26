package com.wanna.logger.impl.event


/**
 * 这是针对于Logger的Event的一个具体实现
 */
open class LoggingEvent(_level: Level, _msg: Any?, _loggerName: String) : ILoggingEvent {
    private var level: Level = _level
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

    override fun getLevel(): Level {
        return level
    }
}