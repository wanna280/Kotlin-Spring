package com.wanna.logger.impl.encoder

/**
 * 日志的Encoder，负责将一个日志事件去进行编码
 */
interface LoggerEncoder<E> {
    fun encode(e: E): String
}