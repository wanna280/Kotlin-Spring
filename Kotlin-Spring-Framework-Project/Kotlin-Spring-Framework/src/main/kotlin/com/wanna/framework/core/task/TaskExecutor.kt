package com.wanna.framework.core.task

import java.util.concurrent.Executor

/**
 * 执行任务的一个`Executor`，对应于JDK当中的`Executor`
 *
 * @see Executor
 */
@FunctionalInterface
interface TaskExecutor : Executor {
    override fun execute(task: Runnable)
}