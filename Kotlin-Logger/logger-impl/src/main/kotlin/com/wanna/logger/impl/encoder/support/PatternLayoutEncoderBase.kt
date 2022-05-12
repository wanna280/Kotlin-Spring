package com.wanna.logger.impl.encoder.support

import com.wanna.logger.impl.event.ILoggingEvent

/**
 * 它在使用Layout的同时，支持了使用pattern去进行Encode
 */
abstract class PatternLayoutEncoderBase<E : ILoggingEvent> : LayoutLoggerEncoderBase<E>() {
}