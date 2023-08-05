package org.slf4j.impl

import org.slf4j.Logger
import org.slf4j.Marker

/**
 * 这是一个Slf4j的桥接Logger, 将自己实现的Logger桥接给Slf4j, 让Slf4j可以调用我自己实现的Logger;
 * 更多的相关功能都沿用自己的Logger, 让Slf4j的API尽可能配合我的Logger去进行实现和使用
 *
 * @see Logger
 */
open class Slf4jBridgeLogcLogger(name: String) : Logger, com.wanna.logger.impl.LogcLogger(name) {
    override fun getName(): String {
        return super.getLoggerName()
    }

    override fun isTraceEnabled(marker: Marker?): Boolean {
        return super.isTraceEnabled()
    }

    override fun isDebugEnabled(marker: Marker?): Boolean {
        return super.isDebugEnabled()
    }

    override fun isErrorEnabled(marker: Marker?): Boolean {
        return super.isErrorEnabled()
    }

    override fun isInfoEnabled(marker: Marker?): Boolean {
        return super.isInfoEnabled()
    }

    override fun trace(format: String, arg: Any?) {
        return super.trace(format(format, arg))
    }

    override fun trace(format: String, arg1: Any?, arg2: Any?) {
        super.trace(format(format, arg1, arg2))
    }

    override fun trace(format: String, vararg arguments: Any?) {
        super.trace(format(format, *arguments))
    }

    override fun trace(msg: String, t: Throwable) {
        super.trace(format(msg))
        t.printStackTrace()
    }

    override fun trace(marker: Marker?, msg: String) {
        super.trace(format(msg))
    }

    override fun trace(marker: Marker?, format: String, arg: Any?) {
        super.trace(format(format, arg))
    }

    override fun trace(marker: Marker?, format: String, arg1: Any?, arg2: Any?) {
        super.trace(format(format, arg1, arg2))
    }

    override fun trace(marker: Marker?, format: String, vararg argArray: Any?) {
        super.trace(format(format, *argArray))
    }

    override fun trace(marker: Marker?, msg: String, t: Throwable) {
        super.trace(format(msg))
        t.printStackTrace()
    }

    override fun debug(format: String, arg: Any?) {
        super.debug(format(format, arg))
    }

    override fun debug(format: String, arg1: Any?, arg2: Any?) {
        super.debug(format(format, arg1, arg2))
    }

    override fun debug(format: String, vararg arguments: Any?) {
        super.debug(format(format, *arguments))
    }

    override fun debug(msg: String, t: Throwable) {
        super.debug(format(msg))
        t.printStackTrace()
    }

    override fun debug(marker: Marker?, msg: String) {
        super.debug(format(msg))
    }

    override fun debug(marker: Marker?, format: String, arg: Any?) {
        super.debug(format(format, arg))
    }

    override fun debug(marker: Marker?, format: String, arg1: Any?, arg2: Any?) {
        super.debug(format(format, arg1, arg2))
    }

    override fun debug(marker: Marker?, format: String, vararg arguments: Any?) {
        super.debug(format(format, *arguments))
    }

    override fun debug(marker: Marker?, msg: String, t: Throwable) {
        super.debug(format(msg))
        t.printStackTrace()
    }

    override fun info(format: String, arg: Any?) {
        super.info(format(format, arg))
    }

    override fun info(format: String, arg1: Any?, arg2: Any?) {
        super.info(format(format, arg1, arg2))
    }

    override fun info(format: String, vararg arguments: Any?) {
        super.info(format(format, *arguments))
    }

    override fun info(msg: String, t: Throwable) {
        super.info(format(msg))
        t.printStackTrace()
    }

    override fun info(marker: Marker?, msg: String) {
        super.info(format(msg))
    }

    override fun info(marker: Marker?, format: String, arg: Any?) {
        super.info(format(format, arg))
    }

    override fun info(marker: Marker?, format: String, arg1: Any?, arg2: Any?) {
        super.info(format(format, arg1, arg2))
    }

    override fun info(marker: Marker?, format: String, vararg arguments: Any?) {
        super.info(format(format, *arguments))
    }

    override fun info(marker: Marker?, msg: String, t: Throwable) {
        super.info(format(msg))
        t.printStackTrace()
    }

    override fun isWarnEnabled(marker: Marker?): Boolean {
        return super.isWarnEnabled()
    }

    override fun warn(format: String, arg: Any?) {
        super.warn(format(format, arg))
    }

    override fun warn(format: String, vararg arguments: Any?) {
        super.warn(format(format, *arguments))
    }

    override fun warn(format: String, arg1: Any?, arg2: Any?) {
        super.warn(format(format, arg1, arg2))
    }

    override fun warn(msg: String, t: Throwable) {
        super.warn(format(msg))
        t.printStackTrace()
    }

    override fun warn(marker: Marker?, msg: String) {
        super.warn(format(msg))
    }

    override fun warn(marker: Marker?, format: String, arg: Any?) {
        super.warn(format(format, arg))
    }

    override fun warn(marker: Marker?, format: String, arg1: Any?, arg2: Any?) {
        super.warn(format(format, arg1, arg2))
    }

    override fun warn(marker: Marker?, format: String, vararg arguments: Any?) {
        super.warn(format(format, *arguments))
    }

    override fun warn(marker: Marker?, msg: String, t: Throwable) {
        super.warn(format(msg))
        t.printStackTrace()
    }

    override fun error(format: String, arg: Any?) {
        super.error(format(format, arg))
    }

    override fun error(format: String, arg1: Any?, arg2: Any?) {
        super.error(format(format, arg1, arg2))
    }

    override fun error(format: String, vararg arguments: Any?) {
        super.error(format(format, *arguments))
    }

    override fun error(msg: String, t: Throwable) {
        super.error(format(msg))
        t.printStackTrace()
    }

    override fun error(marker: Marker?, msg: String) {
        super.error(format(msg))
    }

    override fun error(marker: Marker?, format: String, arg: Any?) {
        super.error(format(format, arg))
    }

    override fun error(marker: Marker?, format: String, arg1: Any?, arg2: Any?) {
        super.error(format(format, arg1, arg2))
    }

    override fun error(marker: Marker?, format: String, vararg arguments: Any?) {
        super.error(format(format, *arguments))
    }

    override fun error(marker: Marker?, msg: String, t: Throwable) {
        super.error(format(msg))
        t.printStackTrace()
    }

    private fun format(format: String, vararg args: Any?): String {
        val builder = StringBuilder(format)
        args.indices.forEach {
            val index = builder.indexOf("{}")
            if (index != -1) {
                builder.replace(index, index + "{}".length, args[it]?.toString() ?: "")
            }
        }
        return builder.toString()
    }

    override fun toString(): String = " Slf4jBridgeLogcLogger[${this.getLoggerName()}]"
}