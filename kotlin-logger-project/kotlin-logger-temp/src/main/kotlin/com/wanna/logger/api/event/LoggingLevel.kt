package com.wanna.logger.api.event

import java.util.*

enum class LoggingLevel(val level: Int) {
    TRACE(10), DEBUG(20), INFO(30), WARN(40), ERROR(50);

    companion object {
        @JvmStatic
        fun parse(level: String): LoggingLevel {
            return valueOf(level.uppercase(Locale.getDefault()))
        }
    }
}