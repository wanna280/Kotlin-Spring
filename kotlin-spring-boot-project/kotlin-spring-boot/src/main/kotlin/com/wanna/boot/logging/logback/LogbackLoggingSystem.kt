package com.wanna.boot.logging.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import com.wanna.boot.logging.AbstractLoggingSystem
import com.wanna.boot.logging.LogLevel
import com.wanna.boot.logging.LoggingSystem
import com.wanna.boot.logging.LoggingSystemFactory
import com.wanna.framework.core.Order
import com.wanna.framework.core.Ordered
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import org.slf4j.LoggerFactory

/**
 * 基于Logback日志组件的[LoggingSystem]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
open class LogbackLoggingSystem(private val classLoader: ClassLoader) : AbstractLoggingSystem() {

    /**
     * 维护LogLevel与Logback的Level之间的映射关系
     */
    private val logLevels = LogLevels<Level>()

    init {
        logLevels.map(LogLevel.FATAL, Level.ERROR)
        logLevels.map(LogLevel.ERROR, Level.ERROR)
        logLevels.map(LogLevel.WARN, Level.WARN)
        logLevels.map(LogLevel.INFO, Level.INFO)
        logLevels.map(LogLevel.DEBUG, Level.DEBUG)
        logLevels.map(LogLevel.TRACE, Level.ALL)
        logLevels.map(LogLevel.TRACE, Level.TRACE)
        logLevels.map(LogLevel.OFF, Level.OFF)
    }


    /**
     * 设置给定loggerName对应的Logger的日志级别
     *
     * @param logLevel system logLevel
     * @param loggerName loggerName
     */
    override fun setLogLevel(loggerName: String, logLevel: LogLevel) {
        val logger = getLogger(loggerName)
        logger.level = logLevels.convertSystemToNative(logLevel)
    }

    /**
     * 根据loggerName, 去获取到Logback日志组件的的[Logger]
     *
     * @param loggerName LoggerName
     * @return Logger
     */
    private fun getLogger(loggerName: String): Logger {
        val loggerContext = getLoggerContext()
        return loggerContext.getLogger(loggerName)
    }

    /**
     * 获取Logback日志组件的[LoggerContext]
     *
     * @return LoggerContext
     */
    private fun getLoggerContext(): LoggerContext {
        val factory = LoggerFactory.getILoggerFactory()
        return factory as LoggerContext
    }

    /**
     * Logback的[LoggingSystemFactory]实现
     */
    @Order(Ordered.ORDER_LOWEST)
    class Factory : LoggingSystemFactory {
        companion object {

            /**
             * 检查Logback是否存在?
             */
            @JvmStatic
            private val PRESENT =
                ClassUtils.isPresent("ch.qos.logback.classic.LoggerContext", Factory::class.java.classLoader)
        }

        /**
         * 获取Logback的[LoggingSystem]
         *
         * @param classLoader ClassLoader
         * @return 如果Logback日志组件存在, return [LogbackLoggingSystem]; 否则return null
         */
        @Nullable
        override fun getLoggingSystem(classLoader: ClassLoader): LoggingSystem? {
            if (PRESENT) {
                return LogbackLoggingSystem(classLoader)
            }
            return null
        }
    }
}