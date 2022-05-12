package com.wanna.logger.impl.encoder.support

/**
 * 它在使用Layout的同时，支持了使用pattern去进行Encode
 */
abstract class PatternLayoutEncoderBase<E> : LayoutLoggerEncoderBase<E>() {

    private var pattern: String? = null

    fun setPattern(pattern: String) {
        this.pattern = pattern
    }

    fun getPattern() : String? = this.pattern
}