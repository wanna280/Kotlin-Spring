package com.wanna.boot

/**
 * 这是一个应用类型的枚举，包括None/Servlet/Reactive三种类型
 */
enum class ApplicationType {
    NONE, SERVLET, REACTIVE;

    companion object {
        /**
         * 从ClassPath当中去推断应用的类型
         */
        fun deduceFromClassPath(): ApplicationType {
            return REACTIVE
        }
    }
}