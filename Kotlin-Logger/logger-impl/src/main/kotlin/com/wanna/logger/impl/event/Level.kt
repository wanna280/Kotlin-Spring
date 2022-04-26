package com.wanna.logger.impl.event

import java.util.*

/**
 * 在实现端，我自己实现我的日志级别枚举Level，我也不遵循api指定方的规范，
 * 因为这个Level也只会在实现方当中被调用到，并不会被api规范的制定方所调用
 */
enum class Level(val level: Int) {
    TRACE(10), DEBUG(20), INFO(30), WARN(40), ERROR(50);

    companion object {
        @JvmStatic
        fun parse(level: String): Level {
            return valueOf(level.uppercase(Locale.getDefault()))
        }
    }
}