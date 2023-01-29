package com.wanna.logger.impl.appender.support

import com.wanna.logger.impl.appender.LoggerAppender
import com.wanna.logger.impl.encoder.LoggerEncoder
import com.wanna.logger.impl.encoder.support.PatternLayoutEncoder
import com.wanna.logger.impl.event.ILoggingEvent
import org.fusesource.jansi.Ansi
import java.io.OutputStream

/**
 * 基于输出流的Appender
 */
abstract class OutputStreamAppender : LoggerAppender {
    // 输出流
    var out: OutputStream? = null

    // Encoder
    var encoder: LoggerEncoder<ILoggingEvent>? = PatternLayoutEncoder()

    override fun append(event: ILoggingEvent) {
        val encoder = this.encoder ?: throw IllegalStateException("请先初始化Encoder")
        val outputStream = this.out ?: throw IllegalStateException("请先初始化输出流OutputStream")
        val byteArray = Ansi.ansi().eraseScreen().render(encoder.encode(event)).toString().toByteArray()
        outputStream.write(byteArray)
    }
}