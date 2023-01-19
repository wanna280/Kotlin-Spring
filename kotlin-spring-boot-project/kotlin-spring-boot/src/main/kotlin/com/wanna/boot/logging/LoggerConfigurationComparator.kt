package com.wanna.boot.logging

/**
 * [LoggerConfiguration]的比较器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 *
 * @param rootLoggerName rootLoggerName
 */
internal class LoggerConfigurationComparator(private val rootLoggerName: String) : Comparator<LoggerConfiguration> {
    override fun compare(o1: LoggerConfiguration, o2: LoggerConfiguration): Int {
        if (rootLoggerName.equals(o1)) {
            return -1
        }
        if (rootLoggerName.equals(o2)) {
            return 1
        }
        return o1.name.compareTo(o2.name)
    }
}