package com.wanna.logger.impl.encoder.support

import com.wanna.logger.impl.event.ILoggingEvent
import com.wanna.logger.impl.layout.LoggerLayout

/**
 * 这是一个基于Layout的Encoder
 */
abstract class LayoutLoggerEncoderBase<E : ILoggingEvent> : LoggerEncoderBase<E>() {

    private var layout: LoggerLayout<E>? = null

    @Suppress("UNCHECKED_CAST")
    open fun setLayout(layout: LoggerLayout<*>) {
        this.layout = layout as LoggerLayout<E>
    }

    open fun getLayout(): LoggerLayout<E>? = this.layout
}