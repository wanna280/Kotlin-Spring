package com.wanna.boot.actuate.logging

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.ReadOperation
import com.wanna.boot.actuate.endpoint.annotation.Selector
import com.wanna.boot.actuate.endpoint.annotation.WriteOperation
import com.wanna.boot.logging.LogLevel
import com.wanna.boot.logging.LoggerConfiguration
import com.wanna.boot.logging.LoggerGroups
import com.wanna.boot.logging.LoggingSystem
import java.util.*
import javax.annotation.Nullable

/**
 * 暴露当前应用的[LoggingSystem]当中的Loggers相关信息的Endpoint
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 *
 * @param loggingSystem SpringBoot LoggingSystem
 * @param loggerGroups Logger的分组情况
 */
@Endpoint("loggers")
open class LoggersEndpoint(private val loggingSystem: LoggingSystem, private val loggerGroups: LoggerGroups) {


    /**
     * 暴露当前应用的[LoggingSystem]当中的全部的Loggers的LogLevel的描述信息的Endpoint
     *
     * @return Descriptor of Loggers
     */
    @ReadOperation
    open fun loggers(): LoggersDescriptor {
        val loggerConfigurations = loggingSystem.getLoggerConfigurations()
        return LoggersDescriptor(getLevels(), loggers(loggerConfigurations), getGroups())
    }

    /**
     * 根据LoggerName/LoggerGroupName, 去获取到该Logger的LogLevel的描述信息
     *
     * @param name loggerName/groupName
     * @return 该Logger/LoggerGroup的日志级别的描述信息
     */
    @Nullable
    @ReadOperation
    open fun loggerLevels(@Selector name: String): LoggerLevelsDescriptor? {
        val loggerGroup = this.loggerGroups.get(name)
        if (loggerGroup != null) {
            return GroupLoggerLevelsDescriptor(loggerGroup.configuredLogLevel, loggerGroup.members)
        }
        val configuration = loggingSystem.getLoggerConfiguration(name)
        return if (configuration != null) SingleLoggerLevelsDescriptor(configuration) else null
    }

    /**
     * 配置给定的Logger/LoggerGroup的LogLevel, 可以实现运行时修改Logger的日志级别的功能
     *
     * @param name loggerName/groupName
     * @param logLevel 要去进行配置的LogLevel
     */
    @WriteOperation
    open fun configureLogLevel(@Selector name: String, @Nullable logLevel: LogLevel?) {
        // 先尝试获取LoggerGroup, 如果存在的话, 那么就去设置LoggerGroup的LogLevel
        val loggerGroup = this.loggerGroups.get(name)
        if (loggerGroup != null && loggerGroup.hasMember()) {
            loggerGroup.configureLogLevel(logLevel, this.loggingSystem::setLogLevel)
            return
        }
        // 如果不是LoggerGroup的话, 那么直接去设置Logger的LogLevel
        this.loggingSystem.setLogLevel(name, logLevel)
    }

    /**
     * 获取到当前的[LoggingSystem]当中所有的支持日志级别
     *
     * @return 支持的日志级别列表
     */
    private fun getLevels(): NavigableSet<LogLevel> {
        return TreeSet(this.loggingSystem.getSupportedLogLevels()).descendingSet()
    }

    /**
     * 获取到所有的Logger的日志级别的配置信息
     *
     * @param configurations LoggerConfigurations
     * @return 所有的Logger的日志级别的描述信息
     */
    private fun loggers(configurations: List<LoggerConfiguration>): Map<String, LoggerLevelsDescriptor> {
        return configurations.associate { it.name to LoggerLevelsDescriptor(it.configuredLevel) }
    }

    /**
     * 获取到所有的分组的Logger的相关信息
     *
     * @return 分组的Logger的描述信息
     */
    private fun getGroups(): Map<String, GroupLoggerLevelsDescriptor> {
        return loggerGroups.associate { it.name to GroupLoggerLevelsDescriptor(it.configuredLogLevel, it.members) }
    }


    /**
     * 全部的Loggers的描述信息
     *
     * @param levels 支持的日志级别
     * @param loggers 所有的Logger的描述信息
     * @param groups 所有的分组Logger的描述信息
     */
    data class LoggersDescriptor(
        val levels: NavigableSet<LogLevel>,
        val loggers: Map<String, LoggerLevelsDescriptor>,
        val groups: Map<String, GroupLoggerLevelsDescriptor>
    )

    /**
     * 单个Logger的日志级别的描述信息
     *
     * @param configuredLevel 被配置成为的日志级别
     */
    open class LoggerLevelsDescriptor(val configuredLevel: LogLevel?) {

        /**
         * 获取到LogLevel的name
         *
         * @param logLevel LogLevel
         * @return name of LogLevel
         */
        fun getName(@Nullable logLevel: LogLevel?): String {
            return logLevel?.name ?: ""
        }
    }

    /**
     * Logger的分组信息的描述信息
     *
     * @param configuredLevel 被配置的日志级别
     * @param members 该分组下的成员Logger
     */
    open class GroupLoggerLevelsDescriptor(configuredLevel: LogLevel?, val members: List<String>) :
        LoggerLevelsDescriptor(configuredLevel)

    /**
     * 单个Logger的描述信息
     */
    open class SingleLoggerLevelsDescriptor(configuration: LoggerConfiguration) :
        LoggerLevelsDescriptor(configuration.configuredLevel) {
        val effectiveLevel = getName(configuration.effectiveLevel)
    }

}