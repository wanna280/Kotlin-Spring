package com.wanna.logger.impl.utils

object ReflectionUtils {
    /**
     * 寻找调用方的相关信息，遍历整个栈轨迹，找到第一个不是以com.wanna.logger包开头的
     */
    @JvmStatic
    fun findCallerInfo(): CallerInfo? {
        for (element in java.lang.RuntimeException().stackTrace) {
            if (!element.className.startsWith("com.wanna.logger")) {
                return CallerInfo(element.className, element.lineNumber, element.methodName)
            }
        }
        return null
    }

    data class CallerInfo(val className: String, val lineNumber: Int, val methodName: String)
}