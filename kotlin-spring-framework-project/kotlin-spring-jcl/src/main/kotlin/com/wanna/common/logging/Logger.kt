package com.wanna.common.logging

/**
 * Logger
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 *
 * @see Logger
 */
interface Logger {

    val isInfoEnabled: Boolean

    val isDebugEnabled: Boolean

    val isWarnEnabled: Boolean

    val isErrorEnabled: Boolean

    val isTraceEnabled: Boolean

    // info相关API

    fun info(msg: String)

    fun info(msg: String, vararg args: Any?)

    fun info(msg: String, ex: Throwable)

    // debug相关API

    fun debug(msg: String)

    fun debug(msg: String, vararg args: Any?)

    fun debug(msg: String, ex: Throwable)

    // warn相关API

    fun warn(msg: String)

    fun warn(msg: String, vararg args: Any?)

    fun warn(msg: String, ex: Throwable)

    // error相关API
    fun error(msg: String)

    fun error(msg: String, vararg args: Any?)

    fun error(msg: String, ex: Throwable)

    // trace相关API

    fun trace(msg: String)

    fun trace(msg: String, vararg args: Any?)

    fun trace(msg: String, ex: Throwable)

}