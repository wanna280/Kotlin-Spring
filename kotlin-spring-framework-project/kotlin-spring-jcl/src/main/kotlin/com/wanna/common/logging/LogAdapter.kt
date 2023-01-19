package com.wanna.common.logging

import org.slf4j.LoggerFactory
import java.io.Serializable

/**
 * Logger Adapter, 负责根据不同的场景, 去创建出来不同的[Logger]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 *
 * @see Logger
 * @see LoggerFactory
 */
object LogAdapter {

    /**
     * 根据LoggerName, 获取到[Logger]
     *
     * @param name loggerName
     * @return Logger
     */
    @JvmStatic
    fun createLogger(name: String): Logger {
        return Slf4jAdapter.createLog(name)
    }


    object Slf4jAdapter {

        /**
         * 根据loggerName, 去创建出来Slf4J包装的[Logger]
         *
         * @param name loggerName
         * @return Logger
         */
        @JvmStatic
        fun createLog(name: String): Logger {
            return Slf4jLogger(LoggerFactory.getLogger(name))
        }
    }

    /**
     * Slf4J的Logger
     *
     * @param logger Slf4J Logger
     */
    private class Slf4jLogger<T : org.slf4j.Logger>(val logger: T) : Logger, Serializable {
        /**
         * loggerName
         */
        val name: String = logger.name

        override val isInfoEnabled: Boolean = logger.isInfoEnabled
        override val isDebugEnabled: Boolean = logger.isDebugEnabled
        override val isWarnEnabled: Boolean = logger.isWarnEnabled
        override val isErrorEnabled: Boolean = logger.isErrorEnabled
        override val isTraceEnabled: Boolean = logger.isTraceEnabled

        override fun info(msg: String) {
            logger.info(msg)
        }

        override fun info(msg: String, vararg args: Any?) {
            logger.info(msg, *args)
        }

        override fun info(msg: String, ex: Throwable) {
            logger.info(msg, ex)
        }

        override fun debug(msg: String) {
            logger.debug(msg)
        }

        override fun debug(msg: String, vararg args: Any?) {
            logger.debug(msg, *args)
        }

        override fun debug(msg: String, ex: Throwable) {
            logger.debug(msg, ex)
        }

        override fun warn(msg: String) {
            logger.warn(msg)
        }

        override fun warn(msg: String, vararg args: Any?) {
            logger.warn(msg, *args)
        }

        override fun warn(msg: String, ex: Throwable) {
            logger.warn(msg, ex)
        }

        override fun error(msg: String) {
            logger.error(msg)
        }

        override fun error(msg: String, vararg args: Any?) {
            logger.error(msg, *args)
        }

        override fun error(msg: String, ex: Throwable) {
            logger.error(msg, ex)
        }

        override fun trace(msg: String) {
            logger.trace(msg)
        }

        override fun trace(msg: String, vararg args: Any?) {
            logger.trace(msg, *args)
        }

        override fun trace(msg: String, ex: Throwable) {
            logger.trace(msg, ex)
        }
    }
}