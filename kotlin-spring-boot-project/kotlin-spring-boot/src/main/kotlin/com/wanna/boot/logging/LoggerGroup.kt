package com.wanna.boot.logging

import com.wanna.framework.lang.Nullable
import java.util.function.BiConsumer

/**
 * 单个Logger的分组, 批量对于多个包下的Logger去进行日志级别的配置
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @see LoggerGroups
 *
 * @param name 当前的分组名
 * @param members 当前分组下的Logger列表
 */
class LoggerGroup(val name: String, val members: List<String>) {

    /**
     * 当前分组已经被配置的LogLevel
     */
    @Nullable
    var configuredLogLevel: LogLevel? = null
        private set

    /**
     * 对当前的分组下的所有Logger去进行日志级别的配置
     *
     * @param logLevel 要去进行使用的LogLevel
     * @param configurer 对于Logger的日志级别去进行配置的Callback回调函数
     */
    fun configureLogLevel(logLevel: LogLevel?, configurer: BiConsumer<String, LogLevel?>) {
        this.configuredLogLevel = logLevel
        members.forEach { configurer.accept(it, logLevel) }
    }

    /**
     * 检查当前的分组下, 是否有Logger成员
     *
     * @return 如果存在有members, return true; 否则return false
     */
    fun hasMember(): Boolean = this.members.isNotEmpty()
}