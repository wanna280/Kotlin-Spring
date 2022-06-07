package com.wanna.framework.scheduling.config

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.scheduling.TaskScheduler
import com.wanna.framework.scheduling.concurrent.ConcurrentTaskScheduler
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * 定时任务的Registrar，负责将任务交给任务调度器去进行执行
 *
 * @see ScheduledExecutorService
 * @see TaskScheduler
 */
open class ScheduledTaskRegistrar : InitializingBean, DisposableBean {
    // 任务调度器
    private var scheduler: TaskScheduler? = null

    // 定时调度的线程池
    private var localExecutor: ScheduledExecutorService? = null

    // fixedDelay的Task任务列表
    private var fixedDelayTasks: MutableList<IntervalTask>? = null

    // fixedRate的Task任务列表
    private var fixedRateTasks: MutableList<IntervalTask>? = null

    // 维护定时调度的任务列表
    private val scheduledTasks = LinkedHashSet<ScheduledTask>()

    open fun setScheduler(scheduler: Any) {
        if (scheduler is TaskScheduler) {
            this.scheduler = scheduler
        } else if (scheduler is ScheduledExecutorService) {
            this.scheduler = ConcurrentTaskScheduler(scheduler)
        } else {
            throw IllegalArgumentException("不支持使用这样的Scheduler")
        }
    }

    open fun getScheduler(): TaskScheduler? {
        return this.scheduler
    }

    /**
     * 在初始化时，应该启动所有的定时任务，因为之前加入时，有可能并未完成任务的启动...
     */
    override fun afterPropertiesSet() {
        if (this.scheduler == null) {
            this.localExecutor = Executors.newSingleThreadScheduledExecutor()
            this.scheduler = ConcurrentTaskScheduler(localExecutor!!)
        }
        fixedDelayTasks?.forEach { scheduledTasks.add(scheduleFixedDelayTask(it)) }
        fixedRateTasks?.forEach { scheduledTasks.add(scheduleFixedRateTask(it)) }
    }

    override fun destroy() {
        this.scheduledTasks.forEach { it.cancel() }
        this.localExecutor?.shutdown()
    }

    /**
     * 添加固定延时(fixedDelay)的任务
     *
     * @param task task
     */
    open fun scheduleFixedDelayTask(task: IntervalTask): ScheduledTask {
        val scheduledTask = ScheduledTask(task)
        val scheduler = this.scheduler
        if (scheduler != null) {
            scheduledTask.future = scheduler.scheduleWithFixedDelay(task.runnable, task.interval)
        } else {
            addFixedDelayTask(task)
        }
        return scheduledTask
    }

    /**
     * 添加固定速率(fixedRate)的任务
     *
     * @param task task
     */
    open fun scheduleFixedRateTask(task: IntervalTask): ScheduledTask {
        val scheduledTask = ScheduledTask(task)
        val scheduler = this.scheduler
        if (scheduler != null) {
            scheduledTask.future = scheduler.scheduleAtFixedRate(task.runnable, task.interval)
        } else {
            addFixedRateTask(task)
        }
        return scheduledTask
    }

    open fun addFixedRateTask(task: Runnable, rate: Long) {
        addFixedRateTask(IntervalTask(task, rate, 0L))
    }

    /**
     * 添加一个任务到fixedRateTasks队列当中
     *
     * @param task 你想要添加的任务
     */
    open fun addFixedRateTask(task: IntervalTask) {
        if (this.fixedRateTasks == null) {
            this.fixedRateTasks = ArrayList(4)
        }
        this.fixedRateTasks?.add(task)
    }

    open fun addFixedDelayTask(task: Runnable, delay: Long) {
        addFixedDelayTask(IntervalTask(task, delay, 0))
    }

    /**
     * 添加一个任务到fixedDelayTasks任务队列当中
     *
     * @param task 你想要添加的任务
     */
    open fun addFixedDelayTask(task: IntervalTask) {
        if (this.fixedDelayTasks == null) {
            this.fixedDelayTasks = ArrayList(4)
        }
        this.fixedDelayTasks?.add(task)
    }
}