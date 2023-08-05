package com.wanna.boot.logging.java

import com.wanna.boot.logging.LoggingSystemProperties
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.logging.Formatter
import java.util.logging.LogRecord

/**
 * JUL的Logger的Formatter的简单实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 */
open class SimpleFormatter : Formatter() {

    companion object {
        /**
         * 默认的日志格式化的Pattern
         */
        private const val DEFAULT_FORMAT =
            "[%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS.%1\$tL] - %8\$s %4\$s [%7\$s] --- %3\$s: %5\$s%6\$s%n"

        /**
         * LogFormat的系统属性Key
         */
        private const val LOG_FORMAT_KEY = "LOG_FORMAT"
    }

    /**
     * 当前时间
     */
    private val date = Date()

    /**
     * 格式化的Pattern
     */
    private val format = getOrUseDefault(LOG_FORMAT_KEY, DEFAULT_FORMAT)

    /**
     * 进程PID
     */
    private val pid = getOrUseDefault(LoggingSystemProperties.PID_KEY, "???")

    /**
     * 格式化给定的[LogRecord]成为一条日志行
     *
     * @param record LogRecord
     * @return 格式化的之后得到的的日志行
     */
    @Synchronized
    override fun format(record: LogRecord): String {
        this.date.time = record.millis
        val source = record.loggerName
        val message = formatMessage(record)
        val throwable = getThrowable(record)
        val threadName = getThreadName()
        return String.format(
            this.format, this.date, source, record.loggerName,
            record.level.localizedName, message, throwable, threadName, pid
        )
    }

    /**
     * 从[LogRecord]当中提取到异常信息
     *
     * @param record record
     * @return 异常信息(无异常的话return "")
     */
    private fun getThrowable(record: LogRecord): String {
        if (record.thrown == null) {
            return ""
        }
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        printWriter.println()
        record.thrown.printStackTrace(printWriter)
        printWriter.close()
        return stringWriter.toString()
    }

    /**
     * 获取到当前线程名
     *
     * @return current thread name
     */
    private fun getThreadName(): String = Thread.currentThread().name ?: ""

    /**
     * 从系统属性当中去获取到Key对应的属性值, 获取不到那么return [defaultValue]
     *
     * @param key 属性Key
     * @param defaultValue 默认值
     * @return 获取到的属性值(或者是默认值)
     */
    private fun getOrUseDefault(key: String, defaultValue: String): String {
        var value: String? = null
        try {
            value = System.getenv(key)
        } catch (ex: Exception) {
            // ignore
        }
        value = value ?: defaultValue
        return System.getProperty(key, value)
    }
}