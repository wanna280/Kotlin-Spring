package com.wanna.boot.logging.logback

import com.wanna.boot.logging.LogFile
import com.wanna.boot.logging.LoggingSystemProperties
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.PropertyResolver
import java.util.function.BiConsumer
import javax.annotation.Nullable

/**
 * Logback日志组件的[LoggingSystemProperties]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 *
 * @param environment Environment
 * @param setter 对于Logger相关的属性值, 需要被设置到哪里? 不指定时, 是将它去设置到SystemProperties当中去
 */
open class LogbackLoggingSystemProperties(environment: ConfigurableEnvironment, setter: BiConsumer<String, String?>) :
    LoggingSystemProperties(environment, setter) {

    override fun apply(@Nullable logFile: LogFile?, propertyResolver: PropertyResolver) {
        super.apply(logFile, propertyResolver)

        applyRollingPolicyProperties(propertyResolver)
    }

    private fun applyRollingPolicyProperties(propertyResolver: PropertyResolver) {

    }
}