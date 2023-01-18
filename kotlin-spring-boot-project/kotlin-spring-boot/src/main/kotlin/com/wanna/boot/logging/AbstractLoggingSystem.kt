package com.wanna.boot.logging

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
abstract class AbstractLoggingSystem : LoggingSystem() {

    /**
     * 维护系统的[LogLevel]与日志组件本地的LogLevel之间的映射关系
     */
    protected class LogLevels<T> {

        private val systemToNative = LinkedHashMap<LogLevel, T>()

        private val nativeToSystem = LinkedHashMap<T, LogLevel>()

        fun map(systemLevel: LogLevel, nativeLevel: T) {
            systemToNative[systemLevel] = nativeLevel
            nativeToSystem[nativeLevel] = systemLevel
        }

        fun convertNativeToSystem(nativeLevel: T): LogLevel {
            return nativeToSystem[nativeLevel] ?: throw IllegalStateException("no such native level")
        }

        fun convertSystemToNative(systemLevel: LogLevel): T {
            return systemToNative[systemLevel] ?: throw IllegalStateException("no such system level")
        }

        fun getSupported(): Set<LogLevel> = systemToNative.keys.toSet()
    }

}