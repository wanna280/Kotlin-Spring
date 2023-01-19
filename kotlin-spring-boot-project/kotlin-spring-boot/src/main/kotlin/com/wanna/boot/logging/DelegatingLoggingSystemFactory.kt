package com.wanna.boot.logging

import com.wanna.framework.lang.Nullable
import java.util.function.Function

/**
 * 通过委托其他的[LoggingSystemFactory]去实现[LoggingSystemFactory]的功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @param delegates 提供[LoggingSystemFactory]的工厂方法
 */
internal class DelegatingLoggingSystemFactory(private val delegates: Function<ClassLoader, List<LoggingSystemFactory>>) :
    LoggingSystemFactory {

    /**
     * 从[delegates]的所有的[LoggingSystemFactory]当中去获取到合适的[LoggingSystem]
     *
     * @param classLoader ClassLoader
     * @return 合适的[LoggingSystem], 如果获取不到合适的, 那么return null
     */
    @Nullable
    override fun getLoggingSystem(classLoader: ClassLoader): LoggingSystem? {
        val delegates = delegates.apply(classLoader)
        for (delegate in delegates) {
            val loggingSystem = delegate.getLoggingSystem(classLoader)
            if (loggingSystem != null) {
                return loggingSystem
            }
        }
        return null
    }
}