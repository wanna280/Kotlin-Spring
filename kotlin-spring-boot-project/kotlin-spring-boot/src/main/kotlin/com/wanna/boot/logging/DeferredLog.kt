package com.wanna.boot.logging

import com.wanna.boot.logging.LogLevel.*
import com.wanna.common.logging.Logger
import com.wanna.framework.lang.Nullable
import java.util.function.Supplier

/**
 * 延时输出的[Logger]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
class DeferredLog(private val destinationSupplier: Supplier<Logger>) : Logger {

    @Volatile
    @Nullable
    private var destination: Logger? = null

    /**
     * 待进行延时输出的日志行列表
     */
    private val lines = Lines()

    @get:Synchronized
    override val isFatalEnabled: Boolean
        get() = destination == null || destination!!.isFatalEnabled

    @get:Synchronized
    override val isErrorEnabled: Boolean
        get() = destination == null || destination!!.isErrorEnabled

    @get:Synchronized
    override val isWarnEnabled: Boolean
        get() = destination == null || destination!!.isWarnEnabled

    @get:Synchronized
    override val isInfoEnabled: Boolean
        get() = destination == null || destination!!.isInfoEnabled

    @get:Synchronized
    override val isDebugEnabled: Boolean
        get() = destination == null || destination!!.isDebugEnabled

    @get:Synchronized
    override val isTraceEnabled: Boolean
        get() = destination == null || destination!!.isTraceEnabled

    @Synchronized
    override fun fatal(msg: String) = log(FATAL, msg, emptyArray(), null)

    @Synchronized
    override fun fatal(msg: String, vararg args: Any?) = log(FATAL, msg, arrayOf(*args), null)

    @Synchronized
    override fun fatal(msg: String, ex: Throwable) = log(FATAL, msg, emptyArray(), ex)

    @Synchronized
    override fun error(msg: String) = log(ERROR, msg, emptyArray(), null)

    @Synchronized
    override fun error(msg: String, vararg args: Any?) = log(ERROR, msg, arrayOf(*args), null)

    @Synchronized
    override fun error(msg: String, ex: Throwable) = log(ERROR, msg, emptyArray(), ex)

    @Synchronized
    override fun info(msg: String) = log(INFO, msg, emptyArray(), null)

    @Synchronized
    override fun info(msg: String, vararg args: Any?) = log(INFO, msg, arrayOf(*args), null)

    @Synchronized
    override fun info(msg: String, ex: Throwable) = log(INFO, msg, emptyArray(), ex)

    @Synchronized
    override fun debug(msg: String) = log(DEBUG, msg, emptyArray(), null)

    @Synchronized
    override fun debug(msg: String, vararg args: Any?) = log(DEBUG, msg, arrayOf(*args), null)

    @Synchronized
    override fun debug(msg: String, ex: Throwable) = log(DEBUG, msg, emptyArray(), ex)

    @Synchronized
    override fun warn(msg: String) = log(WARN, msg, emptyArray(), null)

    @Synchronized
    override fun warn(msg: String, vararg args: Any?) = log(WARN, msg, arrayOf(*args), null)

    @Synchronized
    override fun warn(msg: String, ex: Throwable) = log(WARN, msg, emptyArray(), ex)

    @Synchronized
    override fun trace(msg: String) = log(TRACE, msg, emptyArray(), null)

    @Synchronized
    override fun trace(msg: String, vararg args: Any?) = log(TRACE, msg, arrayOf(*args), null)

    @Synchronized
    override fun trace(msg: String, ex: Throwable) = log(TRACE, msg, emptyArray(), ex)

    fun switchOver() {
        this.destination = destinationSupplier.get()
    }

    @Synchronized
    private fun log(level: LogLevel, message: String, args: Array<Any?>, ex: Throwable?) {
        if (this.destination != null) {
            logTo(this.destination!!, level, message, args, ex)
        } else {
            this.lines.add(destinationSupplier, level, message, args, ex)
        }
    }

    /**
     * 使用Logger去进行输出
     *
     * @param logger logger
     * @param level level
     * @param message message
     * @param args args
     * @param ex ex
     */
    private fun logTo(
        logger: Logger,
        level: LogLevel,
        message: String,
        args: Array<out Any?>,
        @Nullable ex: Throwable?
    ) {
        when (level) {
            FATAL -> logger.fatal(message, *args, ex)
            WARN -> logger.warn(message, *args, ex)
            ERROR -> logger.error(message, *args, ex)
            INFO -> logger.info(message, *args, ex)
            DEBUG -> logger.debug(message, *args, ex)
            TRACE -> logger.trace(message, *args, ex)
            OFF -> {}
        }
    }

    class Lines : Iterable<Line> {

        private val lines = ArrayList<Line>()
        fun add(
            destinationSupplier: Supplier<Logger>,
            level: LogLevel,
            message: String,
            args: Array<Any?>,
            @Nullable throwable: Throwable?
        ) {
            this.lines += Line(destinationSupplier, level, message, args, throwable)
        }

        fun clear() {
            this.lines.clear()
        }

        override fun iterator(): Iterator<Line> = lines.iterator()
    }

    class Line(
        val destinationSupplier: Supplier<Logger>,
        val level: LogLevel,
        val message: String,
        val args: Array<Any?>,
        @Nullable val throwable: Throwable?
    ) {
        val destination: Logger
            get() = destinationSupplier.get()
    }
}