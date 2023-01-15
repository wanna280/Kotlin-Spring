package com.wanna.logger.api.event

/**
 * 这是对LoggingEvent进行的一层抽象, 每一条日志记录, 就是对应了一条LoggingEvent,
 * 但是实际上对于API规范的实现者来说, 它们并不一定需要使用API层的抽象规范, 完全可以自己制定自己的Event规范
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