package com.wanna.boot.actuate.scheduling

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.ReadOperation
import com.wanna.framework.scheduling.config.*
import com.wanna.framework.scheduling.support.ScheduledMethodRunnable
import java.util.function.Function

/**
 * SpringBoot应用当中用于去暴露定时任务的详细信息的Endpoint
 *
 * @param scheduledTaskHolder 提供Spring当中的定时任务列表的ScheduledTasksHolder
 */
@Endpoint("scheduledtasks")
open class ScheduledTasksEndpoint(private val scheduledTaskHolder: ScheduledTaskHolder) {

    /**
     * 暴露一个读取ScheduledTask任务的详细的报告的Endpoint给用户去进行访问
     *
     * @return ScheduledTasksReport(各种各种的定时任务的报告)
     */
    @ReadOperation
    open fun scheduledTasks(): ScheduledTasksReport {
        val scheduledTasks = scheduledTaskHolder.getScheduledTasks()
        val descriptionsByType = HashMap<TaskType, MutableCollection<TaskDescription>>()
        scheduledTasks.forEach {
            val taskDescription = TaskDescription.of(it.task)
            descriptionsByType.putIfAbsent(taskDescription.taskType, ArrayList())
            descriptionsByType[taskDescription.taskType]!!.add(taskDescription)
        }
        return ScheduledTasksReport(descriptionsByType)
    }

    /**
     * ScheduledTasks列表的报告信息, 维护了各种类别的定时任务的列表
     *
     * @param descriptionsByType key-TaskType, value-TaskDefinition列表
     */
    class ScheduledTasksReport(descriptionsByType: Map<TaskType, Collection<TaskDescription>>) {
        val cron: Collection<TaskDescription> = emptyList()
        val fixedDelay: Collection<TaskDescription> = descriptionsByType[TaskType.FIXED_DELAY] ?: emptyList()
        val fixedRate: Collection<TaskDescription> = descriptionsByType[TaskType.FIXED_RATE] ?: emptyList()
        val custom: Collection<TaskDescription> = emptyList()
    }

    /**
     * 描述了一个Spring的定时任务的具体信息
     *
     * @param taskType taskType
     * @param runnable Runnable的描述信息
     */
    open class TaskDescription(val taskType: TaskType, val runnable: RunnableDescription) {
        companion object {
            private val DESCRIPTORS = LinkedHashMap<Class<out Task>, Function<Task, TaskDescription>>()

            init {
                DESCRIPTORS[FixedRateTask::class.java] = Function { FixedRateTaskDescription(it as FixedRateTask) }
                DESCRIPTORS[FixedDelayTask::class.java] = Function { FixedDelayTaskDescription(it as FixedDelayTask) }
            }

            /**
             * 给定一个Task, 协助去构建一个TaskDescription
             *
             * @return TaskDescription
             */
            @JvmStatic
            fun of(task: Task): TaskDescription {
                return DESCRIPTORS[task::class.java]!!.apply(task)
            }
        }
    }

    /**
     * Spring的间隔任务的描述信息, 内部维护了初始化延时时间(initialDelay)以及任务的间隔时间(interval)
     *
     * @param task task
     * @param taskType taskType
     */
    open class IntervalTaskDescription(taskType: TaskType, task: IntervalTask) :
        TaskDescription(taskType, RunnableDescription(task.runnable)) {
        var initialDelay = task.initialDelay
        var interval = task.interval
    }

    /**
     * fixedDelay的TaskDescription
     *
     * @param fixedDelayTask task
     */
    open class FixedDelayTaskDescription(fixedDelayTask: FixedDelayTask) :
        IntervalTaskDescription(TaskType.FIXED_DELAY, fixedDelayTask)

    /**
     * fixedRate的TaskDescription
     *
     * @param fixedRateTask task
     */
    open class FixedRateTaskDescription(fixedRateTask: FixedRateTask) :
        IntervalTaskDescription(TaskType.FIXED_RATE, fixedRateTask)

    /**
     * 对一个定时任务的Runnable的描述信息
     *
     * @param runnable 要去进行描述的Runnable
     */
    open class RunnableDescription(runnable: Runnable) {
        var target: String = ""

        init {
            if (runnable is ScheduledMethodRunnable) {
                this.target = runnable.target::class.java.name + "." + runnable.method.name
            } else {
                this.target = runnable::class.java.name
            }
        }
    }

    /**
     * 定时任务类型的枚举值
     */
    enum class TaskType { CUSTOM, CUSTOM_TRIGGER, FIXED_DELAY, FIXED_RATE }
}