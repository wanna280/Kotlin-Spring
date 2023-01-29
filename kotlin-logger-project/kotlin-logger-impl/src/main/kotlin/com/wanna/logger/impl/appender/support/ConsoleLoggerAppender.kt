package com.wanna.logger.impl.appender.support

import java.io.OutputStream

/**
 * 这是一个命令行的日志Appender, 负责将日志输出到Console
 */
open class ConsoleLoggerAppender : OutputStreamAppender() {

    companion object {
        val systemOut: OutputStream = System.out
        val systemError: OutputStream = System.err
    }

    init {
        this.out = systemOut
    }
}