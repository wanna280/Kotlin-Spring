package com.wanna.boot.logging

import com.wanna.boot.logging.DeferredLog.Lines
import com.wanna.boot.logging.LogLevel.*
import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.lang.Nullable
import java.util.function.Supplier

/**
 * 提供日志的延时输出的[Logger], 因为SpringApplication有可能当前情况下, [Logger]还并未准备好
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @param destinationSupplier 获取到用于去进行日志输出的[Logger]的回调函数
 * @param lines 延时输出的日志行列表. 如果不指定的话, 将会使用每个[DeferredLog]一个[Lines]; 如果指定的话, 那么可以针对多个[DeferredLog]使用同一个[Lines]
 */
class DeferredLog(
    @Nullable private val destinationSupplier: Supplier<Logger>? = null,
    @Nullable lines: Lines? = null
) : Logger {

    constructor() : this(null, null)

    /**
     * 用于进行最终的日志输出的Logger, 当延时日志已经全部输出完成之后,
     * 就无需再次使用[DeferredLog]去进行存储日志行, 直接使用目标[Logger]去进行输出即可.
     */
    @Volatile
    @Nullable
    private var destination: Logger? = null

    /**
     * 待进行延时输出的日志行列表
     */
    private val lines = lines ?: Lines()

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

    /**
     * 执行当前的[DeferredLog]日志输出的模式模式切换, 将延时输出的[DeferredLog]切换成为正常非延时的[Logger]的日志输出
     */
    fun switchOver() {
        this.destination = destinationSupplier?.get() ?: throw IllegalStateException("Cannot get destination Logger")
    }

    /**
     * 将当前的[DeferredLog]当中的全部日志信息, 切换到使用目标[Logger]去进行输出,
     * 并将[DeferredLog]的模式去转换成为非延时输出的[Logger],
     * 对于切换之后后续的所有日志, 都无需再次使用到延时输出
     *
     * @param destination 要去进行输出的目标Logger的loggerName
     */
    fun switchTo(destination: Class<*>) = switchTo(LoggerFactory.getLogger(destination))

    /**
     * 将当前的[DeferredLog]当中的全部日志信息, 切换到使用目标[Logger]去进行输出, 实现日志的重定向,
     * 并将[DeferredLog]的模式去转换成为非延时输出的[Logger]
     *
     * @param destination 要去进行输出的目标Logger
     */
    fun switchTo(destination: Logger) {
        synchronized(this.lines) {
            replayTo(destination)
            this.destination = destination
        }
    }

    /**
     * 将当前的[DeferredLog]当中的全部日志信息, 切换到使用目标[Logger]去进行输出, 实现日志的重定向
     *
     * @param destination 要去进行日志输出的目标Logger
     */
    fun replayTo(destination: Logger) {
        synchronized(this.lines) {
            // 使用目标Logger去对所有的延时输出的日志行去进行输出
            for (line in this.lines) {
                logTo(destination, line.level, line.message, line.args, line.throwable)
            }
            this.lines.clear()   // clear
        }
    }

    /**
     * 添加/输出一条日志:
     *
     * * 1.在Logger已经完成初始化时, 将会使用到[Logger]去进行直接输出,
     * * 2.在Logger还未完成初始化时, 将会往[lines]当中去添加一条Log, 等待后续再去进行输出.
     *
     * @param level 日志级别
     * @param message 要去进行输出的日志消息
     * @param args 用于进行日志消息的格式化的参数列表
     * @param ex 异常信息
     */
    @Synchronized
    private fun log(level: LogLevel, message: String, args: Array<Any?>, @Nullable ex: Throwable?) {
        if (this.destination != null) {
            logTo(this.destination!!, level, message, args, ex)
        } else {
            this.lines.add(destinationSupplier, level, message, args, ex)
        }
    }

    companion object {
        /**
         * 如果给定的[source]是[DeferredLog]的话, 那么将该[DeferredLog]当中的所有的日志信息, 交给[destination]去进行输出
         *
         * @param source source Logger
         * @param destination destination Logger
         * @return destination Logger
         */
        @JvmStatic
        fun replay(source: Logger, destination: Class<*>): Logger =
            replay(source, LoggerFactory.getLogger(destination))

        /**
         * 如果给定的[source]是[DeferredLog]的话, 那么将该[DeferredLog]当中的所有的日志信息, 交给[destination]去进行输出
         *
         * @param source source Logger
         * @param destination destination Logger
         * @return destination Logger
         */
        @JvmStatic
        fun replay(source: Logger, destination: Logger): Logger {
            if (source is DeferredLog) {
                source.replayTo(destination)
            }
            return destination
        }

        /**
         * 使用给定的[Logger]去进行输出日志
         *
         * @param logger 进行日志输出的Logger
         * @param level 该条日志信息的日志级别
         * @param message 要去进行日志输出的日志消息
         * @param args 日志参数信息, 用于进行日志消息的格式化
         * @param ex 异常信息
         */
        @JvmStatic
        fun logTo(
            logger: Logger,
            level: LogLevel,
            message: String,
            args: Array<Any?>,
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
    }

    /**
     * 维护[DeferredLog]要去进行延时输出的日志行列表
     *
     * @see Line
     */
    class Lines : Iterable<Line> {

        private val lines = ArrayList<Line>()

        /**
         * 添加一条日志行到当前[Lines]当中, 等待后续的日志输出
         *
         * @param destinationSupplier destinationLoggerSupplier
         * @param level logLevel
         * @param message logMessage
         * @param args args
         * @param throwable Throwable
         */
        fun add(
            @Nullable destinationSupplier: Supplier<Logger>?,
            level: LogLevel,
            message: String,
            args: Array<Any?>,
            @Nullable throwable: Throwable?
        ) {
            this.lines += Line(destinationSupplier, level, message, args, throwable)
        }

        /**
         * 清空掉当前[Lines]当中的所有日志行
         */
        fun clear() = this.lines.clear()

        override fun iterator(): Iterator<Line> = lines.iterator()
    }

    /**
     * 对于单个待进行输出的日志行的描述
     *
     * @param destinationSupplier 进行日志的输出的Logger的Supplier回调函数
     * @param level 日志级别
     * @param message 日志消息
     * @param args 进行日志消息的格式的参数列表
     * @param throwable 日志消息输出时用到的异常信息
     */
    class Line(
        @Nullable val destinationSupplier: Supplier<Logger>?,
        val level: LogLevel,
        val message: String,
        val args: Array<Any?>,
        @Nullable val throwable: Throwable?
    ) {
        /**
         * 获取到用于进行最终日志输出的[Logger]
         */
        val destination: Logger
            get() = destinationSupplier?.get() ?: throw IllegalStateException("Cannot get destination Logger")
    }
}