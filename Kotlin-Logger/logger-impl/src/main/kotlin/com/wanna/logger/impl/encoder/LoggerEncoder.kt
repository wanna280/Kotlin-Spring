package com.wanna.logger.impl.encoder

import com.wanna.logger.impl.event.ILoggingEvent

/**
 * 日志的Encoder，负责将一个日志事件去进行编码
 */
interface LoggerEncoder<E : ILoggingEvent> {
    fun encode(e: E): String
}