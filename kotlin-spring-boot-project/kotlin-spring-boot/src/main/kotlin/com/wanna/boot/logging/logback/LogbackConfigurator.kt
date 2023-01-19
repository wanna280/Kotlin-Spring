package com.wanna.boot.logging.logback

import ch.qos.logback.classic.LoggerContext

/**
 * Logback的Configurator, 实现对于Logback日志组件的自定义
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/19
 *
 * @param context LoggerContext
 */
open class LogbackConfigurator(val context: LoggerContext) {

    /**
     * 获取到执行Logback的配置的锁
     */
    val getConfigurationLock: Any
        get() = context.configurationLock

}