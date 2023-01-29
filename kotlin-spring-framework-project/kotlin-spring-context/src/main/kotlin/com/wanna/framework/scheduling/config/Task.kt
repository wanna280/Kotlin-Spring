package com.wanna.framework.scheduling.config

open class Task(val runnable: Runnable) {
    override fun toString() = "Task(runnable=$runnable)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Task
        if (runnable != other.runnable) return false
        return true
    }

    override fun hashCode() = runnable.hashCode()
}