package com.wanna.framework.core.task

import java.util.concurrent.Executor

@FunctionalInterface
interface TaskExecutor : Executor {
    override fun execute(task: Runnable)
}