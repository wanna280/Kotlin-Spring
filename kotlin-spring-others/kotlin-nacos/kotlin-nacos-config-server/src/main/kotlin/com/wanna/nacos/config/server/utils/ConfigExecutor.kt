package com.wanna.nacos.config.server.utils

import com.wanna.nacos.api.common.executor.NameThreadFactory
import java.util.concurrent.Future
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Nacos ConfigServer相关的全局线程池
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
object ConfigExecutor {

    /**
     * 用于执行长轮询任务的线程池
     */
    private val LONG_POLLING_EXECUTOR =
        ScheduledThreadPoolExecutor(1, NameThreadFactory("com.wanna.nacos.config.LongPolling"))

    /**
     * schedule一个长轮询任务, 并返回一个Future
     *
     * @param runnable 长轮询任务Runnable
     * @param period 时间间隔
     * @param unit 时间单位
     */
    @JvmStatic
    fun scheduleLongPolling(runnable: Runnable, period: Long, unit: TimeUnit): Future<*> {
        return LONG_POLLING_EXECUTOR.schedule(runnable, period, unit)
    }

    /**
     * 提交一个长轮询任务
     *
     * @param runnable 长轮询任务Runnable
     */
    @JvmStatic
    fun executeLongPolling(runnable: Runnable) {
        LONG_POLLING_EXECUTOR.execute(runnable)
    }
}