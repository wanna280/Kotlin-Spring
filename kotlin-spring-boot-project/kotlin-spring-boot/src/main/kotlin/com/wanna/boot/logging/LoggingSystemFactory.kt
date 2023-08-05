package com.wanna.boot.logging

import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.lang.Nullable

/**
 * [LoggingSystem]的工厂, 通过[getLoggingSystem]方法去获取到真正的[LoggingSystem]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
fun interface LoggingSystemFactory {

    /**
     * 获取到合适的[LoggingSystem]的实现, 如果没有合适的[LoggingSystem]实现, return null
     *
     * @param classLoader ClassLoader to use
     * @return suitable LoggingSystem
     */
    @Nullable
    fun getLoggingSystem(classLoader: ClassLoader): LoggingSystem?

    companion object {

        /**
         * 从SpringFactories当中去加载到[LoggingSystemFactory], 并包装成为[DelegatingLoggingSystemFactory]
         *
         * @return DelegatingLoggingSystemFactory
         */
        @JvmStatic
        fun fromSpringFactories(): LoggingSystemFactory =
            DelegatingLoggingSystemFactory { SpringFactoriesLoader.loadFactories(LoggingSystemFactory::class.java, it) }
    }

}