package com.wanna.logger.impl

/**
 * 这是针对与LoggerContext的默认实现类, 只需要提供创建Logger的方式即可, 其余均沿用父类当中的模板
 *
 * @see AbstractLoggerContext
 */
open class DefaultLoggerContext : AbstractLoggerContext<LogcLogger>() {
    override fun newLogger(name: String): LogcLogger {
        return LogcLogger(name)
    }
}