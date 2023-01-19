package com.wanna.boot.logging

/**
 * 对于单个Logger的配置信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
data class LoggerConfiguration(val name: String, val configuredLevel: LogLevel, val effectiveLevel: LogLevel)