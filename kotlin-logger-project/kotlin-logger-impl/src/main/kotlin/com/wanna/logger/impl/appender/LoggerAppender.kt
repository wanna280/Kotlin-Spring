package com.wanna.logger.impl.appender

import com.wanna.logger.impl.event.ILoggingEvent


/**
 * 这是一个Logger的Appender, 负责将msg以IO流的方式进行写出
 */
interface LoggerAppender {
    fun append(event: ILoggingEvent)
}