package com.wanna.logger.impl.utils

class ReflectionUtils {
    companion object {
        /**
         * 寻找调用方的ClassName，遍历整个栈轨迹，找到第一个不是以com.wanna.logger包开头的
         */
        @JvmStatic
        fun findCallerClassName(): String? {
            for (element in java.lang.RuntimeException().stackTrace) {
                if (!element.className.startsWith("com.wanna.logger")) {
                    return element.className
                }
            }
            return null
        }
    }
}