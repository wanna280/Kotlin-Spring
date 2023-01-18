package com.wanna.boot.context.logging

import com.wanna.boot.context.event.ApplicationEnvironmentPreparedEvent
import com.wanna.boot.context.event.ApplicationStartingEvent
import com.wanna.boot.logging.LoggingSystem
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.GenericApplicationListener
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils

/**
 * Logging的处理的[GenericApplicationListener], 提供对于[LoggingSystem]的自动配置
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
open class LoggingApplicationListener : GenericApplicationListener {

    @Nullable
    private var loggingSystem: LoggingSystem? = null


    override fun onApplicationEvent(event: ApplicationEvent) {
        if (event is ApplicationStartingEvent) {
            onApplicationStartingEvent(event)
        }
        if (event is ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPreparedEvent(event)
        }
    }

    private fun onApplicationStartingEvent(event: ApplicationStartingEvent) {
        val loggingSystem = LoggingSystem.get(event.application.getClassLoader())
        loggingSystem.beforeInitialize()
        this.loggingSystem = loggingSystem
    }

    private fun onApplicationEnvironmentPreparedEvent(event: ApplicationEnvironmentPreparedEvent) {
        if (this.loggingSystem == null) {
            this.loggingSystem = LoggingSystem.get(event.application.getClassLoader())
        }

        // 完成LoggingSystem的初始化
        initialize(event.environment, event.application.getClassLoader())
    }

    protected open fun initialize(environment: ConfigurableEnvironment, classLoader: ClassLoader) {

    }

    override fun supportsEventType(type: ResolvableType): Boolean {
        val rawClass = type.resolveType().getRawClass()
        return ClassUtils.isAssignFrom(ApplicationStartingEvent::class.java, rawClass)
                || ClassUtils.isAssignFrom(ApplicationEnvironmentPreparedEvent::class.java, rawClass)
    }
}