package com.wanna.framework.core.task.support

import com.wanna.framework.core.task.AsyncTaskExecutor
import com.wanna.framework.core.task.TaskExecutor
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * TaskExecutor的Adapter，负责将juc的Executor适配到Spring的AsyncTaskExecutor；
 * 因为Spring当中需要用到AsyncTaskExecutor，而一般使用到是juc包下的Executor
 *
 * @param executor juc的Executor
 */
open class TaskExecutorAdapter(private val executor: Executor) : AsyncTaskExecutor {
    override fun submit(task: Runnable): Future<*> {
        if (executor is ExecutorService) {
            return executor.submit(task)
        } else if (executor is AsyncTaskExecutor) {
            return executor.submit(task)
        }
        throw IllegalStateException("不支持使用这样的Executor")
    }

    override fun <T> submit(task: Callable<T>): Future<T> {
        if (executor is ExecutorService) {
            return executor.submit(task)
        } else if (executor is AsyncTaskExecutor) {
            return executor.submit(task)
        }
        throw IllegalStateException("不支持使用这样的Executor")
    }

    override fun execute(task: Runnable) = executor.execute(task)
}