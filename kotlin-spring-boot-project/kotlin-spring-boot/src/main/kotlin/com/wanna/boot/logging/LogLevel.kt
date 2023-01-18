package com.wanna.boot.logging

/**
 * 针对各种类型的日志组件的日志级别提供的一层抽象, 为[LoggingSystem]提供支持
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF
}