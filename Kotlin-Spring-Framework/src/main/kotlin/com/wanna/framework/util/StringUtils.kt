package com.wanna.framework.util

class StringUtils {
    companion object {
        /**
         * 判断一个字符串是否有文本，判断长度是否为0
         */
        fun hasText(str: String?): Boolean {
            return str == null || str.isEmpty()
        }
    }
}