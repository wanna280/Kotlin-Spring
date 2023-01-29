package com.wanna.logger.api.event

/**
 * 这是对LoggingEvent进行的一层抽象, 每一条日志记录, 就是对应了一条LoggingEvent
 */
interface LoggingEvent {

    fun getThreadId(): Long

    fun getThreadName(): String

    fun getMessage(): String?

    fun getTimestamp(): Long

    fun getThrowable(): Throwable?

    fun getLoggerName(): String

    fun getLevel(): LoggingLevel
}