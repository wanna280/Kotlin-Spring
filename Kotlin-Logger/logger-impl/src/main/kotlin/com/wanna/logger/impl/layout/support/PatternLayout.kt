package com.wanna.logger.impl.layout.support

import com.wanna.logger.impl.event.LoggingEvent
import com.wanna.logger.impl.layout.LoggerLayout

open class PatternLayout : LoggerLayout<LoggingEvent> {

    override fun doLayout(e: LoggingEvent): String {
        TODO("Not yet implemented")
    }
}