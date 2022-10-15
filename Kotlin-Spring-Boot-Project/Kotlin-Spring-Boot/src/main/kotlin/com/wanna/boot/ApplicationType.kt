package com.wanna.boot

import com.wanna.framework.util.ClassUtils

/**
 * 这是一个应用类型的枚举，包括None/Servlet/Reactive三种类型
 */
enum class ApplicationType {
    NONE, SERVLET, MVC;

    companion object {
        private const val NETTY_MARKER = "io.netty.bootstrap.ServerBootstrap"

        private const val DISPATCHER_HANDLER_MARKER = "com.wanna.framework.web.DispatcherHandler"

        /**
         * 从ClassPath当中去推断应用的类型
         */
        @JvmStatic
        fun deduceFromClassPath(): ApplicationType {
            if (ClassUtils.isPresent(NETTY_MARKER) && ClassUtils.isPresent(DISPATCHER_HANDLER_MARKER)) {
                return MVC
            }
            return NONE
        }
    }
}