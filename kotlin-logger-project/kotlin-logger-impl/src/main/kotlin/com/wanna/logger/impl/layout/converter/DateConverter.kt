package com.wanna.logger.impl.layout.converter

import com.wanna.logger.impl.event.ILoggingEvent
import java.text.SimpleDateFormat
import java.util.*

class DateConverter : Converter<ILoggingEvent> {
    override fun convert(expression: String, event: ILoggingEvent): String {
        val left = expression.indexOf('{')
        val right = expression.indexOf('}')
        val datePattern = expression.substring(left + 1, right)
        return SimpleDateFormat(datePattern).format(Date(event.getTimestamp()))
    }
}