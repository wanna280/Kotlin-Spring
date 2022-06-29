package com.wanna.framework.scheduling.concurrent

import com.wanna.framework.scheduling.TaskScheduler
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 并发任务的调度器
 *
 * @param executorService 要使用的线程池
 */
open class ConcurrentTaskScheduler(private val executorService: ScheduledExecutorService) : TaskScheduler {
    private val unit = TimeUnit.MILLISECONDS

    override fun scheduleWithFixedDelay(task: Runnable, delay: Long): ScheduledFuture<*> {
        return scheduleWithFixedDelay(task, Date(System.currentTimeMillis()), delay)
    }

    override fun scheduleWithFixedDelay(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*> {
        val initDelay = startTime.time - System.currentTimeMillis()
        return executorService.scheduleWithFixedDelay(task, initDelay, delay, unit)
    }

    override fun scheduleAtFixedRate(task: Runnable, delay: Long): ScheduledFuture<*> {
        return scheduleAtFixedRate(task, Date(System.currentTimeMillis()), delay)
    }

    override fun scheduleAtFixedRate(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*> {
        val initDelay = startTime.time - System.currentTimeMillis()
        return executorService.scheduleAtFixedRate(task, initDelay, delay, unit)
    }
}