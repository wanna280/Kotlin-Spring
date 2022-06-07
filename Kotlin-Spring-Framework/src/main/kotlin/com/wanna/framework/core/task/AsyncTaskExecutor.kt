package com.wanna.framework.core.task

import java.util.concurrent.Callable
import java.util.concurrent.Future

/**
 * 异步任务的执行器
 */
interface AsyncTaskExecutor : TaskExecutor {
    fun submit(task: Runnable): Future<*>
    fun <T> submit(task: Callable<T>): Future<T>
}