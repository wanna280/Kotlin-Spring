package com.wanna.boot.logging

import com.wanna.boot.context.logging.LoggingApplicationListener
import javax.annotation.Nullable

/**
 * LoggerGroups, 对于Logger的分组, 可以基于"logging.level"配置, 去实现对于不同的包的配置去进行分组,
 * 例如在配置文件当中去进行下面这样的配置"logging.group.wanna=com.wanna.controller,com.wanna.user",
 * 那么就代表了将"com.wanna.controller"和"com.wanna.user"都去划分到"wanna"分组下,
 * 就可以使用"logging.level.wanna=INFO"这样的配置, 去实现将"com.wanna.controller"和"com.wanna.user"的日志级别的配置
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @see LoggingApplicationListener.loggerGroups
 * @see LoggingApplicationListener.LOGGING_GROUP
 * @see LoggingApplicationListener.DEFAULT_GROUP_LOGGERS
 */
class LoggerGroups() : Iterable<LoggerGroup> {

    /**
     * 当前LoggerGroups下维护的多个分组的配置信息, Key-分组名, Value-LoggerGroup
     */
    private val groups = LinkedHashMap<String, LoggerGroup>()

    /**
     * 基于初始的分组信息去构建[LoggerGroups]
     *
     * @param namesAndMembers 需要添加的Logger的分组情况, Key-分组名, Value该分组对应的Logger列表
     */
    constructor(namesAndMembers: Map<String, List<String>>) : this() {
        putAll(namesAndMembers)
    }

    /**
     * 批量添加多个[LoggerGroup]
     *
     * @param namesAndMembers groups, Key-分组名, Value-该分组下的Logger列表
     */
    fun putAll(namesAndMembers: Map<String, List<String>>) {
        namesAndMembers.forEach(this::put)
    }

    /**
     * 添加一个[LoggerGroup]的配置
     *
     * @param name 分组名
     * @param members 该分组下的Logger列表
     */
    fun put(name: String, members: List<String>) {
        this.groups[name] = LoggerGroup(name, members)
    }

    /**
     * 添加一个[LoggerGroup]配置
     *
     * @param group 要去进行添加的LoggerGroup
     */
    fun put(group: LoggerGroup) {
        this.groups[group.name] = group
    }

    /**
     * 根据Group, 去获取到该分组对应的[LoggerGroup]
     *
     * @param name groupName
     * @return 获取到的[LoggerGroup], 不存在这样的group的话, return null
     */
    @Nullable
    fun get(name: String): LoggerGroup? = groups[name]

    /**
     * 迭代所有的[LoggerGroups]
     *
     * @return LoggerGroup的迭代
     */
    override fun iterator(): Iterator<LoggerGroup> = groups.values.iterator()
}