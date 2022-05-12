package com.wanna.logger.impl.encoder.support

import com.wanna.logger.impl.layout.LoggerLayout

/**
 * 这是一个基于Layout的Encoder
 */
abstract class LayoutLoggerEncoderBase<E> : LoggerEncoderBase<E>() {

    private var layout: LoggerLayout<E>? = null

    fun setLayout(layout: LoggerLayout<E>) {
        this.layout = layout
    }

    fun getLayout(): LoggerLayout<E>? = this.layout
}