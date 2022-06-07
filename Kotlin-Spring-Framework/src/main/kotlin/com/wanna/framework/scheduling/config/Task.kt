package com.wanna.framework.scheduling.config

open class Task(val runnable: Runnable) {
    override fun toString(): String {
        return "Task(runnable=$runnable)"
    }
}