package com.wanna.framework.core.task

import java.util.concurrent.Callable
import java.util.concurrent.Future

/**
 * 异步任务的执行器, 相比于`TaskExecutor`, 新增了提交异步任务的方式
 * 对于异步任务的方式, 提交给`TaskExecutor`一个任务, 会返回一个`Future`给用户去进行保存
 *
 * @see Future
 * @see TaskExecutor
 */
interface AsyncTaskExecutor : TaskExecutor {

    /**
     * 提交一个任务给线程池执行
     *
     * @param task 要提交给线程池执行的任务
     * @return 执行提交任务的Future
     */
    fun submit(task: Runnable): Future<*>

    /**
     * 提交一个任务给线程池执行
     *
     * @param task 要提交给线程池执行的任务
     * @return 执行提交任务的Future
     */
    fun <T> submit(task: Callable<T>): Future<T>
}