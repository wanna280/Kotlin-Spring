package com.wanna.logger.impl.appender

import com.wanna.logger.impl.event.ILoggingEvent
import com.wanna.logger.impl.utils.DateFormatter
import com.wanna.logger.impl.utils.ReflectionUtils
import org.fusesource.jansi.Ansi.ansi
import java.io.OutputStream

/**
 * 这是一个命令行的日志Appender，负责将日志输出到Console
 */
open class ConsoleLoggerAppender : LoggerAppender {
    private val out: OutputStream = System.out
    private val err: OutputStream = System.err

    override fun append(event: ILoggingEvent) {
        out.write(getLoggerString(event).toByteArray())
    }

    private fun getLoggerString(event: ILoggingEvent): String {
        val clazzName = ReflectionUtils.findCallerClassName()
        val time = DateFormatter.format(event.getTimestamp())
        return ansi().eraseScreen()
            .render("@|black $time|@ @|green ${event.getLevel().name}|@ @|blue ${event.getThreadId()}|@ --- @|black [${event.getThreadName()}]|@ @|magenta $clazzName|@ : @|black ${event.getMessage()}|@ \n")
            .toString()
    }
}