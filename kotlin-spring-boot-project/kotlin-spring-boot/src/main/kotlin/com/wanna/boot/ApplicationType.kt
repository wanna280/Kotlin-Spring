package com.wanna.boot

import com.wanna.framework.util.ClassUtils

/**
 * 这是一个应用类型的枚举, 包括None/Servlet/Mvc三种类型
 */
enum class ApplicationType {
    NONE, SERVLET, MVC;

    companion object {

        /**
         * Servlet的标识类
         */
        private const val SERVLET_MARKER = "javax.servlet.Servlet"

        /**
         * DispatcherServlet的标识类
         */
        private const val DISPATCHER_SERVLET_MARKER = "com.wanna.framework.web.server.servlet.DispatcherServlet"

        /**
         * Servlet环境下的ApplicationContext
         */
        private const val SERVLET_APPLICATION_CONTEXT =
            "com.wanna.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext"

        /**
         * Netty的标识类
         */
        private const val NETTY_MARKER = "io.netty.bootstrap.ServerBootstrap"

        /**
         * DispatcherHandler的标识类
         */
        private const val DISPATCHER_HANDLER_MARKER = "com.wanna.framework.web.DispatcherHandler"

        /**
         * 从ClassPath当中去推断应用的类型
         */
        @JvmStatic
        fun deduceFromClassPath(): ApplicationType {
            // 如果包含有DispatcherHandler的话, 那么才需要去进行探测...不然return NONE
            if (ClassUtils.isPresent(DISPATCHER_HANDLER_MARKER)) {
                // 探测当前是否是Servlet环境
                if (ClassUtils.isPresent(SERVLET_MARKER)
                    && ClassUtils.isPresent(DISPATCHER_SERVLET_MARKER)
                    && ClassUtils.isPresent(SERVLET_APPLICATION_CONTEXT)
                ) {
                    return SERVLET
                }
                // 探测当前是否是Netty环境
                if (ClassUtils.isPresent(NETTY_MARKER)) {
                    return MVC
                }
            }
            return NONE
        }
    }
}