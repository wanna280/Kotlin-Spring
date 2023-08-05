package com.wanna.framework.core.task

import com.wanna.framework.util.concurrent.ListenableFuture
import java.util.concurrent.Callable

/**
 * 在AsyncTaskExecutor提供的基础功能上, 新增支持提交一个异步任务, 并且返回一个ListenableFuture的方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/20
 */
interface AsyncListenableTaskExecutor : AsyncTaskExecutor {
    /**
     * 提交一个任务给线程池执行
     *
     * @param task 要提交给线程池执行的任务
     * @return 执行提交任务的ListenableFuture
     * @see ListenableFuture
     */
    fun submitListenable(task: Runnable): ListenableFuture<*>

    /**
     * 提交一个任务给线程池执行
     *
     * @param task 要提交给线程池执行的任务
     * @return 执行提交任务的ListenableFuture
     * @param T Callable方法的返回值类型
     * @see Callable.call
     * @see ListenableFuture
     */
    fun <T> submitListenable(task: Callable<T>): ListenableFuture<T>
}