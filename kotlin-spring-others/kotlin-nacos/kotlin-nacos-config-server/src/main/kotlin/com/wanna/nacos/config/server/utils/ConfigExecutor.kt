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
    @JvmStatic
    private val LONG_POLLING_EXECUTOR =
        ScheduledThreadPoolExecutor(1, NameThreadFactory("com.wanna.nacos.config.LongPolling"))

    /**
     * 用于去进行异步通知的线程池
     */
    @JvmStatic
    private val ASYNC_NOTIFY_EXECUTOR =
        ScheduledThreadPoolExecutor(100, NameThreadFactory("com.wanna.nacos.config.AsyncNotifyService"))

    /**
     * TimerExecutpr
     */
    @JvmStatic
    private val TIMER_EXECUTOR =
        ScheduledThreadPoolExecutor(10, NameThreadFactory("com.wanna.nacos.config.server.timer"))

    /**
     * 执行一个ConfigTask
     *
     * @param command command
     * @param initialDelay 初始delay
     * @param delay delay
     * @param unit 时间单位
     */
    @JvmStatic
    fun scheduleConfigTask(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) {
        TIMER_EXECUTOR.scheduleWithFixedDelay(command, initialDelay, delay, unit)
    }

    /**
     * 执行一次异步的Notify任务
     *
     * @param runnable 异步Notify任务
     */
    @JvmStatic
    fun executeAsyncNotify(runnable: Runnable) {
        ASYNC_NOTIFY_EXECUTOR.execute(runnable)
    }

    /**
     * schedule一次异步的Notify任务
     *
     * @param runnable 异步Notify任务
     * @param period 延时时间
     * @param unit 时间单位
     */
    @JvmStatic
    fun scheduleAsyncNotify(runnable: Runnable, period: Long, unit: TimeUnit) {
        ASYNC_NOTIFY_EXECUTOR.schedule(runnable, period, unit)
    }

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