package com.wanna.framework.scheduling.concurrent

import com.wanna.framework.core.task.TaskRejectedException
import com.wanna.framework.scheduling.TaskScheduler
import java.time.Clock
import java.util.*
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 并发任务的调度器
 *
 * @param executorService 要使用的线程池
 */
open class ConcurrentTaskScheduler(private val executorService: ScheduledExecutorService) : TaskScheduler {

    /**
     * 获取一个默认时区的时钟
     */
    private var clock = Clock.systemDefaultZone()

    override fun scheduleWithFixedDelay(task: Runnable, delay: Long): ScheduledFuture<*> {
        try {
            return executorService.scheduleAtFixedRate(task, 0, delay, TimeUnit.MILLISECONDS)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${executorService}]所拒绝掉", ex)
        }
    }

    override fun scheduleWithFixedDelay(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*> {
        try {
            val initDelay = startTime.time - clock.millis()
            return executorService.scheduleAtFixedRate(task, initDelay, delay, TimeUnit.MILLISECONDS)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${executorService}]所拒绝掉", ex)
        }
    }

    override fun scheduleAtFixedRate(task: Runnable, delay: Long): ScheduledFuture<*> {
        try {
            return executorService.scheduleWithFixedDelay(task, 0, delay, TimeUnit.MILLISECONDS)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${executorService}]所拒绝掉", ex)
        }
    }

    override fun scheduleAtFixedRate(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*> {
        try {
            val initDelay = startTime.time - clock.millis()
            return executorService.scheduleWithFixedDelay(task, initDelay, delay, TimeUnit.MILLISECONDS)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${executorService}]所拒绝掉", ex)
        }
    }
}