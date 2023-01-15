package com.wanna.logger.api

/**
 * 这是顶层的API规范, 实现方想要整合到API当中, 就必须实现这层API规范, 实现这个接口, 并提供相应的实现
 * 这层是门面的Logger, 交给用户去进行在业务当中去进行使用的日志Logger
 */
interface Logger {
    fun info(msg: String)

    fun debug(msg: String)

    fun isDebugEnabled(): Boolean

    fun warn(msg: String)

    fun error(msg: String)

    fun trace(msg: String)

    fun isTraceEnabled(): Boolean

    fun getParent(): Logger?

    fun setParent(logger: Logger)

    fun getLoggerName(): String

    fun setLoggerName(name: String)
}