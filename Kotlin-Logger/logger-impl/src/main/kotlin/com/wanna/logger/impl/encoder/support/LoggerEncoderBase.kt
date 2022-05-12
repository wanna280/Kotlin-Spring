package com.wanna.logger.impl.encoder.support

import com.wanna.logger.impl.encoder.LoggerEncoder
import com.wanna.logger.impl.event.ILoggingEvent

abstract class LoggerEncoderBase<E : ILoggingEvent> : LoggerEncoder<E> {

}