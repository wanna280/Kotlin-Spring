package com.wanna.logger.api.event

import java.util.*

/**
 * 这是在API规范层面提供的LoggingLevel，实现方也完全可以不遵守这个规范，直接使用自己的框架提供的LoggingLevel规范即可
 */
enum class LoggingLevel(val level: Int) {
    TRACE(10), DEBUG(20), INFO(30), WARN(40), ERROR(50);

    companion object {
        @JvmStatic
        fun parse(level: String): LoggingLevel {
            return valueOf(level.uppercase(Locale.getDefault()))
        }
    }
}