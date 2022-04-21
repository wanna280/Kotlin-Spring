package com.wanna.logger.impl.utils

import java.text.SimpleDateFormat
import java.util.Date

class DateFormatter {
    companion object {
        @JvmField
        val formatter = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("yyyy-MM-dd hh:mm:sss")
            }
        }

        @JvmStatic
        fun format(date: Date): String {
            return formatter.get().format(date)
        }

        @JvmStatic
        fun format(timestamp: Long): String {
            return formatter.get().format(Date(timestamp))
        }
    }
}