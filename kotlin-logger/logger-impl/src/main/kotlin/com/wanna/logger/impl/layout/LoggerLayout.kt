package com.wanna.logger.impl.layout

import com.wanna.logger.impl.event.ILoggingEvent

interface LoggerLayout<E : ILoggingEvent> {
    fun doLayout(e: E) : String
}