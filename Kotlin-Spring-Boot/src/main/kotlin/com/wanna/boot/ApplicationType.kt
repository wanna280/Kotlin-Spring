package com.wanna.boot

import com.wanna.framework.core.util.ClassUtils

/**
 * 这是一个应用类型的枚举，包括None/Servlet/Reactive三种类型
 */
enum class ApplicationType {
    NONE, SERVLET, REACTIVE;

    companion object {
        private const val NETTY_MARKER = "io.netty.bootstrap.ServerBootstrap"

        /**
         * 从ClassPath当中去推断应用的类型
         */
        fun deduceFromClassPath(): ApplicationType {
            if (ClassUtils.isPresent(NETTY_MARKER, ApplicationType::class.java.classLoader)) {
                return REACTIVE
            }
            return NONE
        }
    }
}