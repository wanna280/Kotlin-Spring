package com.wanna.logger.impl.utils

import java.text.SimpleDateFormat
import java.util.Date

/**
 * 这是一个日期格式化工具类
 */
object DateFormatter {
    private val formatter = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("yyyy-MM-dd hh:mm:sss")
        }
    }

    /**
     * 将Date类型的日期对象去进行格式化
     */
    @JvmStatic
    fun format(date: Date): String {
        return formatter.get().format(date)
    }

    /**
     * 将Long类型的时间戳去进行格式化
     */
    @JvmStatic
    fun format(timestamp: Long): String {
        return formatter.get().format(Date(timestamp))
    }
}