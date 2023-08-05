package com.wanna.common.logging

import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.function.Function
import java.util.logging.Level
import java.util.logging.LogRecord
import javax.annotation.Nullable

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
     * Slf4j API是否存在?
     */
    @JvmStatic
    private val slf4jApiPresent = isPresent("org.slf4j.Logger")

    /**
     * 创建Logger用到的函数
     */
    @JvmStatic
    private val createLogFunc: Function<String, Logger>

    init {
        if (slf4jApiPresent) {
            createLogFunc = Function { Slf4jAdapter.createLog(it) }

            // fallback 使用JUL作为Logger
        } else {
            createLogFunc = Function { JavaUtilAdapter.createLog(it) }
        }
    }

    /**
     * 根据LoggerName, 获取到[Logger]
     *
     * @param name loggerName
     * @return Logger
     */
    @JvmStatic
    fun createLogger(name: String): Logger = createLogFunc.apply(name)

    /**
     * 检查给定的className对应的类的依赖是否存在?
     *
     * @param className className
     * @return 如果存在, return true; 否则return false
     */
    private fun isPresent(className: String): Boolean {
        return try {
            Class.forName(className, false, LogAdapter::class.java.classLoader)
            true
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * Slf4j的Adapter
     */

    private object Slf4jAdapter {

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

        override val isFatalEnabled: Boolean
            get() = logger.isErrorEnabled

        override val isErrorEnabled: Boolean
            get() = logger.isErrorEnabled

        override val isWarnEnabled: Boolean
            get() = logger.isWarnEnabled
        override val isInfoEnabled: Boolean
            get() = logger.isInfoEnabled

        override val isDebugEnabled: Boolean
            get() = logger.isDebugEnabled

        override val isTraceEnabled: Boolean
            get() = logger.isTraceEnabled

        override fun fatal(msg: String) = error(msg)

        override fun fatal(msg: String, vararg args: Any?) = error(msg, *args)

        override fun fatal(msg: String, ex: Throwable) = error(msg, ex)

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

    private object JavaUtilAdapter {
        @JvmStatic
        fun createLog(name: String): Logger = JavaUtilLogger(name)
    }

    /**
     * 将JUL的Logger桥接到Logger当中来
     */
    class JavaUtilLogger(name: String) : Logger, Serializable {

        private val logger: java.util.logging.Logger = java.util.logging.Logger.getLogger(name)

        private val name: String = logger.name

        override val isFatalEnabled: Boolean
            get() = isErrorEnabled
        override val isErrorEnabled: Boolean
            get() = logger.isLoggable(Level.SEVERE)
        override val isInfoEnabled: Boolean
            get() = logger.isLoggable(Level.INFO)
        override val isDebugEnabled: Boolean
            get() = logger.isLoggable(Level.FINE)
        override val isWarnEnabled: Boolean
            get() = logger.isLoggable(Level.WARNING)

        override val isTraceEnabled: Boolean
            get() = logger.isLoggable(Level.FINEST)

        override fun fatal(msg: String) = error(msg)

        override fun fatal(msg: String, vararg args: Any?) = error(msg, *args)

        override fun fatal(msg: String, ex: Throwable) = error(msg, ex)

        override fun error(msg: String) = error(msg, *emptyArray())

        override fun error(msg: String, ex: Throwable) = error(msg, *arrayOf(ex))

        override fun error(msg: String, vararg args: Any?) = log(Level.SEVERE, msg, args = args)

        override fun info(msg: String) = info(msg, args = emptyArray())

        override fun info(msg: String, ex: Throwable) = info(msg, arrayOf(ex))

        override fun info(msg: String, vararg args: Any?) = log(Level.INFO, msg, args = args)

        override fun debug(msg: String) = debug(msg, *emptyArray())

        override fun debug(msg: String, ex: Throwable) = debug(msg, *arrayOf(ex))

        override fun debug(msg: String, vararg args: Any?) = log(Level.FINE, msg, args = args)

        override fun warn(msg: String) = warn(msg, *emptyArray())

        override fun warn(msg: String, ex: Throwable) = warn(msg, *arrayOf(ex))

        override fun warn(msg: String, vararg args: Any?) = log(Level.WARNING, msg, args = args)

        override fun trace(msg: String) = trace(msg, *emptyArray())

        override fun trace(msg: String, ex: Throwable) = trace(msg, *arrayOf(ex))

        override fun trace(msg: String, vararg args: Any?) = log(Level.FINEST, msg, args = args)

        /**
         * 执行Log的输出
         *
         * @param level LogLevel
         * @param message message to output
         * @param args 格式化message需要用到的参数信息, 支持去进行占位符的解析和替换
         */
        private fun log(level: Level, message: String, vararg args: Any?) {
            if (logger.isLoggable(level)) {
                val messageBuilder = StringBuilder(message)
                for (arg in args) {
                    val index = messageBuilder.indexOf("{}")
                    if (index != -1) {
                        messageBuilder.replace(index, index + 2, arg.toString())
                    }
                }
                val logRecord = LogRecord(level, messageBuilder.toString())
                logRecord.loggerName = logger.name
                logRecord.resourceBundle = logRecord.resourceBundle
                logRecord.resourceBundleName = logRecord.resourceBundleName
                logRecord.thrown = determineThrowable(args = args)
                logger.log(logRecord)
            }
        }

        @Nullable
        private fun determineThrowable(vararg args: Any?): Throwable? {
            if (args.isEmpty()) {
                return null
            }
            if (args[args.size - 1] is Throwable) {
                return args[args.size - 1] as Throwable
            }
            return null
        }
    }
}