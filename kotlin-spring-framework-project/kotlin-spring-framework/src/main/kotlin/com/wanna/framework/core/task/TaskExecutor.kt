package com.wanna.framework.core.task

import java.util.concurrent.Executor

/**
 * 执行任务的一个`Executor`线程池，对应于JDK的juc包当中的`Executor`
 *
 * @see Executor
 */
@FunctionalInterface
fun interface TaskExecutor : Executor {

    /**
     * 提交一个Runnable任务给线程池去进行执行
     *
     * @param task 要提交的Runnable任务
     */
    override fun execute(task: Runnable)
}