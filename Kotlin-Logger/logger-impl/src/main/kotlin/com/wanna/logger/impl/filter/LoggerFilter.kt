package com.wanna.logger.impl.filter

import com.wanna.logger.impl.LogcLogger
import com.wanna.logger.impl.event.Level

/**
 * 这是一个日志的Filter，决定某次日志要不要进行输出？
 */
interface LoggerFilter {
    fun decide(
        logger: LogcLogger,
        level: Level,
        msg: Any?,
        params: Array<Any?>,
        throwable: Throwable?
    ): FilterReply
}