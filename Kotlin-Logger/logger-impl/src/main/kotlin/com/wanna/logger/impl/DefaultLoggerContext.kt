package com.wanna.logger.impl

import java.util.concurrent.ConcurrentHashMap

/**
 * 这是针对与LoggerContext的默认实现类
 */
open class DefaultLoggerContext : AbstractLoggerContext<LogcLogger>() {
    override fun newLoggerCache(): MutableMap<String, LogcLogger> {
        return ConcurrentHashMap<String, LogcLogger>()
    }

    override fun newLogger(name: String): LogcLogger {
        return LogcLogger(name)
    }
}