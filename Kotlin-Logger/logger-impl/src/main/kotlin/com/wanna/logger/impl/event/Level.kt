package com.wanna.logger.impl.event

import java.util.*

/**
 * 在实现端我自己实现我的日志级别枚举Level，我也不完全可以遵循api指定方的规范，
 * 因为这个Level也只会在实现方当中被调用到，并不会被api规范的制定方所调用
 *
 * Level是数字(level)越大，level越高
 */
enum class Level(val level: Int) {
    TRACE(1000), DEBUG(2000), INFO(3000), WARN(4000), ERROR(5000);

    companion object {

        /**
         * 给定字符串，去解析成为Level对象，需要将levelStr转换成为大写字母，才能去使用valueOf方法，因此传递的levelStr不用管是大小写的
         *
         * @param levelStr level String，不分大小写
         * @return 解析出来的Level
         */
        @JvmStatic
        fun parse(levelStr: String): Level {
            return valueOf(levelStr.uppercase(Locale.getDefault()))
        }
    }
}