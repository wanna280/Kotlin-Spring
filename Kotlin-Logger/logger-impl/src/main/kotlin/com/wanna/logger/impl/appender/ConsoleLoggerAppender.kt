package com.wanna.logger.impl.appender

import com.wanna.logger.api.event.LoggingEvent
import org.fusesource.jansi.Ansi.ansi
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 这是一个命令行的日志Appender
 */
open class ConsoleLoggerAppender : LoggerAppender {

    // DateFormat
    private val formatter = ThreadLocal<SimpleDateFormat>()

    init {
        formatter.set(SimpleDateFormat("yyyy-MM-dd hh:mm:sss"))
    }

    private val out: OutputStream = System.out
    private val err: OutputStream = System.err

    override fun append(event: LoggingEvent) {
        out.write(getLoggerString(event).toByteArray())
    }

    private fun getLoggerString(event: LoggingEvent): String {
        val clazzName = findCallerClassName()
        val time = formatter.get().format(Date(event.getTimestamp()))
        return ansi().eraseScreen()
            .render("@|black $time|@ @|green ${event.getLevel().name}|@ @|blue ${event.getThreadId()}|@ --- @|black [${event.getThreadName()}]|@ @|magenta $clazzName|@ : @|black ${event.getMessage()}|@ \n")
            .toString()
    }

    /**
     * 寻找调用方的ClassName，遍历整个栈轨迹，找到第一个不是以com.wanna.logger包开头的
     */
    private fun findCallerClassName(): String? {
        for (element in java.lang.RuntimeException().stackTrace) {
            if (!element.className.startsWith("com.wanna.logger")) {
                return element.className
            }
        }
        return null
    }
}