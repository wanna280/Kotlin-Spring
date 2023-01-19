package com.wanna.boot.logging

import com.wanna.framework.lang.Nullable

/**
 * 对于单个Logger的日志级别的配置信息去进行描述
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @param name loggerName
 * @param configuredLevel 被配置的LogLevel
 * @param effectiveLevel 有效的LogLevel
 */
data class LoggerConfiguration(
    val name: String,
    @Nullable val configuredLevel: LogLevel?,
    @Nullable val effectiveLevel: LogLevel?
)