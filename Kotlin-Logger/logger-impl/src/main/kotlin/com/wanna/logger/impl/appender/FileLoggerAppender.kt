package com.wanna.logger.impl.appender

import com.wanna.logger.impl.event.ILoggingEvent
import com.wanna.logger.impl.utils.DateFormatter
import com.wanna.logger.impl.utils.ReflectionUtils
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * 这是一个基于文件的LoggerAppender，负责将日志输出到文件
 */
open class FileLoggerAppender : LoggerAppender {

    private var logFilePath: String = "log.log"
    private var out: OutputStream? = null

    fun setLogFilePath(path: String) {
        this.logFilePath = path
    }

    override fun append(event: ILoggingEvent) {
        out = FileOutputStream(logFilePath, true)
        out!!.write(getLoggerString(event).toByteArray())
    }

    private fun getLoggerString(event: ILoggingEvent): String {
        val clazzName = ReflectionUtils.findCallerClassName()
        val time = DateFormatter.format(event.getTimestamp())
        return "$time ${event.getLevel().name} ${event.getThreadId()} --- [${event.getThreadName()}]  $clazzName :  ${event.getMessage()} \n"
    }
}