package com.wanna.logger.impl.event


/**
 * 这是对LoggingEvent进行的一层抽象, 每一条日志记录, 就是对应了一条LoggingEvent, 对于实现方来说, 我不一定要使用api模块当中的LoggingEvent,
 * 因为LoggingEvent的调用只是在我实现方的代码当中被调用, 并不会被api规范的指定方所调用到
 */
interface ILoggingEvent {

    fun getThreadId(): Long

    fun getThreadName(): String

    fun getMessage(): String?

    fun getTimestamp(): Long

    fun getThrowable(): Throwable?

    fun getLoggerName(): String

    fun getLevel(): Level
}