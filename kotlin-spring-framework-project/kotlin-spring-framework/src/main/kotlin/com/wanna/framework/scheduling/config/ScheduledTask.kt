package com.wanna.framework.scheduling.config

import java.util.concurrent.Future

/**
 * 交给Spring去进行定时调度的任务
 *
 * @param task 要去进行定时调度的任务
 * @param future 取消任务用到的future(默认为null)
 */
open class ScheduledTask(val task: Task, var future: Future<*>? = null) {

    open fun cancel() {
        this.future?.cancel(true)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ScheduledTask
        if (task != other.task) return false
        return true
    }

    override fun hashCode() = task.hashCode()
}