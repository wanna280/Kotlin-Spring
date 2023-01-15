package com.wanna.framework.scheduling

import java.util.Date
import java.util.concurrent.ScheduledFuture

/**
 * Spring的任务调度器, 对应于juc的ScheduledExecutorService
 *
 * @see java.util.concurrent.ScheduledExecutorService
 * @see ScheduledFuture
 */
interface TaskScheduler {

    /**
     * 使用固定的delay的定时调度
     *
     * @param task 要提交的定时任务
     * @param startTime 开始时间？
     * @param delay 延时的时间
     */
    fun scheduleWithFixedDelay(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*>

    /**
     * 使用固定的delay的定时调度
     *
     * @param task 要提交的定时任务
     * @param delay 延时的时间
     */
    fun scheduleWithFixedDelay(task: Runnable, delay: Long): ScheduledFuture<*>

    /**
     * 使用固定的rate的定时调度
     *
     * @param task 要提交的定时任务
     */
    fun scheduleAtFixedRate(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*>

    /**
     * 使用固定的rate的定时调度
     *
     * @param task 要提交的定时任务
     */
    fun scheduleAtFixedRate(task: Runnable, delay: Long): ScheduledFuture<*>
}