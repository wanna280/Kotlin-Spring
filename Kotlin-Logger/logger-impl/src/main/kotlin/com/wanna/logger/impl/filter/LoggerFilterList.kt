package com.wanna.logger.impl.filter

import com.wanna.logger.impl.LogcLogger
import com.wanna.logger.impl.event.Level
import java.util.concurrent.CopyOnWriteArrayList

open class LoggerFilterList() : CopyOnWriteArrayList<LoggerFilter>() {
    fun getFilterChainDecisionReply(
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
            val loggerFilters = toTypedArray()
            // 遍历所有的Filter，去进行决策，只要其中返回DENY/ACCEPT，那么就return
            // 如果Filter返回了NEUTRAL的话，那么使用下一个Filter去进行继续匹配
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