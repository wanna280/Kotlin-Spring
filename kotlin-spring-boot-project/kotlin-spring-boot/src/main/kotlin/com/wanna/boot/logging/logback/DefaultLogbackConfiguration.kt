package com.wanna.boot.logging.logback

import com.wanna.boot.logging.LogFile
import com.wanna.framework.lang.Nullable

/**
 * 默认的Logback的配置信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/19
 *
 * @param logFile LogFile
 */
internal class DefaultLogbackConfiguration(@Nullable private val logFile: LogFile?) {

    fun apply(config: LogbackConfigurator) {
        synchronized(config.getConfigurationLock) {
            // apply默认的配置信息
            defaults(config)


        }
    }

    private fun defaults(config: LogbackConfigurator) {

    }

}