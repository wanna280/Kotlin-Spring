package org.slf4j.impl

import org.slf4j.Logger
import org.slf4j.Marker

/**
 * 这是一个Slf4j的桥接Logger，将自己实现的Logger桥接给Slf4j，让Slf4j可以调用我自己实现的Logger；
 * 更多的相关功能都沿用自己的Logger，让Slf4j的API尽可能配合我的Logger去进行实现和使用
 *
 * @see Logger
 */
open class Slf4jBridgeLogcLogger(name: String) : Logger, com.wanna.logger.impl.LogcLogger(name) {
    override fun getName(): String {
        return super.getLoggerName()
    }

    override fun isTraceEnabled(marker: Marker): Boolean {
        return super.isTraceEnabled()
    }

    override fun isDebugEnabled(marker: Marker): Boolean {
        return super.isDebugEnabled()
    }

    override fun isErrorEnabled(marker: Marker): Boolean {
        return super.isErrorEnabled()
    }

    override fun isInfoEnabled(marker: Marker): Boolean {
        return super.isInfoEnabled()
    }

    override fun trace(format: String, arg: Any) {
        return super.trace(format)
    }

    override fun trace(format: String, arg1: Any, arg2: Any) {
        super.trace(format)
    }

    override fun trace(format: String, vararg arguments: Any) {
        super.trace(format)
    }

    override fun trace(msg: String, t: Throwable) {
        super.trace(msg)
    }

    override fun trace(marker: Marker, msg: String) {
        super.trace(msg)
    }

    override fun trace(marker: Marker, format: String, arg: Any) {
        super.trace(format)
    }

    override fun trace(marker: Marker, format: String, arg1: Any, arg2: Any) {
        super.trace(format)
    }

    override fun trace(marker: Marker, format: String, vararg argArray: Any) {
        super.trace(format)
    }

    override fun trace(marker: Marker, msg: String, t: Throwable) {
        super.trace(msg)
    }

    override fun debug(format: String, arg: Any) {
        super.debug(format)
    }

    override fun debug(format: String, arg1: Any, arg2: Any) {
        super.debug(format)
    }

    override fun debug(format: String, vararg arguments: Any) {
        super.debug(format)
    }

    override fun debug(msg: String, t: Throwable) {
        super.debug(msg)
    }

    override fun debug(marker: Marker, msg: String) {
        super.debug(msg)
    }

    override fun debug(marker: Marker, format: String, arg: Any) {
        super.debug(format)
    }

    override fun debug(marker: Marker, format: String, arg1: Any, arg2: Any) {
        super.debug(format)
    }

    override fun debug(marker: Marker, format: String, vararg arguments: Any) {
        super.debug(format)
    }

    override fun debug(marker: Marker, msg: String, t: Throwable) {
        super.debug(msg)
    }

    override fun info(format: String, arg: Any) {
        super.info(format)
    }

    override fun info(format: String, arg1: Any, arg2: Any) {
        super.info(format)
    }

    override fun info(format: String, vararg arguments: Any) {
        super.info(format)
    }

    override fun info(msg: String, t: Throwable) {
        super.info(msg)
    }

    override fun info(marker: Marker, msg: String) {
        super.info(msg)
    }

    override fun info(marker: Marker, format: String, arg: Any) {
        super.info(format)
    }

    override fun info(marker: Marker, format: String, arg1: Any, arg2: Any) {
        super.info(format)
    }

    override fun info(marker: Marker, format: String, vararg arguments: Any) {
        super.info(format)
    }

    override fun info(marker: Marker, msg: String, t: Throwable) {
        return super.info(msg)
    }

    override fun isWarnEnabled(marker: Marker): Boolean {
        return super.isWarnEnabled()
    }

    override fun warn(format: String, arg: Any) {
        return super.warn(format)
    }

    override fun warn(format: String, vararg arguments: Any) {
        return super.warn(format)
    }

    override fun warn(format: String, arg1: Any, arg2: Any) {
        return super.warn(format)
    }

    override fun warn(msg: String, t: Throwable) {
        return super.warn(msg)
    }

    override fun warn(marker: Marker, msg: String) {
        return super.warn(msg)
    }

    override fun warn(marker: Marker, format: String, arg: Any) {
        return super.warn(format)
    }

    override fun warn(marker: Marker, format: String, arg1: Any, arg2: Any) {
        return super.warn(format)
    }

    override fun warn(marker: Marker, format: String, vararg arguments: Any) {
        return super.warn(format)
    }

    override fun warn(marker: Marker, msg: String, t: Throwable) {
        return super.warn(msg)
    }


    override fun error(format: String, arg: Any) {
        super.error(format)
    }

    override fun error(format: String, arg1: Any, arg2: Any) {
        super.error(format)
    }

    override fun error(format: String, vararg arguments: Any) {
        super.error(format)
    }

    override fun error(msg: String, t: Throwable) {
        super.error(msg)
    }

    override fun error(marker: Marker, msg: String) {
        super.error(msg)
    }

    override fun error(marker: Marker, format: String, arg: Any) {
        super.error(format)
    }

    override fun error(marker: Marker, format: String, arg1: Any, arg2: Any) {
        super.error(format)
    }

    override fun error(marker: Marker, format: String, vararg arguments: Any) {
        super.error(format)
    }

    override fun error(marker: Marker, msg: String, t: Throwable) {
        super.error(msg)
    }
}