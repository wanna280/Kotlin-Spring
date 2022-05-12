package com.wanna.logger.impl.filter

import com.wanna.logger.impl.LogcLogger
import com.wanna.logger.impl.event.Level
import java.util.concurrent.CopyOnWriteArrayList

/**
 * LoggerFilter列表，使用COW原则去保证线程安全
 */
open class LoggerFilterList : CopyOnWriteArrayList<LoggerFilter>() {

    /**
     * 遍历所有的LoggerFilter，去决策当前的msg日志消息是否需要去进行输出
     *
     * @param logger Logger
     * @param level 当前日志消息的日志登记
     * @param msg 日志消息
     * @param params 参数列表
     * @param throwable 抛出的异常信息
     * @return Filter的决策：ACCEPT/NEUTRAL/DENY
     */
    open fun getFilterChainDecisionReply(
        logger: LogcLogger, level: Level, msg: Any?, params: Array<Any?>, throwable: Throwable?
    ): FilterReply {

        // 如果size==0，那么直接return ACCEPT
        if (size == 0) {
            return FilterReply.ACCEPT
        }
        if (size == 1) {
            return try {
                iterator().next().decide(logger, level, msg, params, throwable)
            } catch (ex: IndexOutOfBoundsException) {
                // 如果抛出了IndexOutOfBoundException，在运行时出现了Filter被移除的情况，说明发生了数组越界的情况，返回NEUTRAL
                FilterReply.NEUTRAL
            }
        } else {
            val loggerFilters = toTypedArray()  // toTypeArray，获取这一时刻的FilterList
            // 遍历所有的Filter，去进行决策，只要其中一个返回DENY/ACCEPT，那么就return
            // 如果Filter返回了NEUTRAL的话，那么需要使用下一个Filter去进行继续匹配
            for (loggerFilter in loggerFilters) {
                val reply = loggerFilter.decide(logger, level, msg, params, throwable)
                if (reply == FilterReply.DENY || reply == FilterReply.ACCEPT) {
                    return reply
                }
            }
        }
        return FilterReply.NEUTRAL
    }
}