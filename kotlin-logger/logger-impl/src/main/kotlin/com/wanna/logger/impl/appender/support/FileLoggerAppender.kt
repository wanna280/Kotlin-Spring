package com.wanna.logger.impl.appender.support

import java.io.FileOutputStream

/**
 * 这是一个基于文件的LoggerAppender，负责将日志输出到文件
 */
open class FileLoggerAppender : OutputStreamAppender() {

    companion object {
        const val DEFAULT_LOG_FILE = "log.log"
    }

    private var logFilePath: String = DEFAULT_LOG_FILE

    init {
        this.out = FileOutputStream(logFilePath, true)
    }

    fun setLogFilePath(path: String) {
        this.logFilePath = path
        this.out = FileOutputStream(path, true)
    }
}