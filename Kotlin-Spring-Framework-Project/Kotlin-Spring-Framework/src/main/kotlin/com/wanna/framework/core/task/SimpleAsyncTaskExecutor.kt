package com.wanna.framework.core.task

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 简单的异步任务的执行器，每次来个任务都创建一个新的线程去执行异步任务，支持使用ThreadFactory/threadNamePrefix两种方式去进行创建
 *
 * @param threadNamePrefix 线程名的前缀(和ThreadFactory冲突)
 * @param threadFactory 执行异步任务时，创建线程应该使用的ThreadFactory(和threadNamePrefix冲突)
 */
open class SimpleAsyncTaskExecutor private constructor(
    threadNamePrefix: String? = null,
    private val threadFactory: ThreadFactory? = null
) : AsyncTaskExecutor {

    // 提供基于"threadNamePrefix"的构造函数
    constructor(threadNamePrefix: String) : this(threadNamePrefix, null)

    // 提供基于"threadFactory"的构造函数
    constructor(threadFactory: ThreadFactory) : this(null, threadFactory)

    // 统计当前已经创建的线程数量，用来生成threadName
    private val threadCount = AtomicInteger(0);

    // 是否要使用守护线程？默认为false
    var daemon: Boolean = false

    // threadNamePrefix
    private val threadNamePrefix: String = threadNamePrefix ?: (SimpleAsyncTaskExecutor::class.java.name + "-")

    /**
     * 提交一个任务给线程池去执行
     *
     * @param task 要提交的任务(Runnable)
     * @return 提交任务之后返回的Future，Future的结果为null
     */
    override fun submit(task: Runnable): Future<*> {
        val future = FutureTask(task, null)
        execute(future)
        return future
    }

    /**
     * 提交一个任务给线程池去执行
     *
     * @param task 要提交的任务(Callable)
     * @return 提交任务之后返回的Future
     */
    override fun <T> submit(task: Callable<T>): Future<T> {
        val future = FutureTask(task)
        execute(future);
        return future;
    }

    /**
     * 提交给线程池去执行一个任务
     *
     * @param task 要提交给线程的任务
     */
    override fun execute(task: Runnable) {
        val thread = if (threadFactory != null) threadFactory.newThread(task) else createThread(task)
        thread.start()
    }

    /**
     * 创建一个线程，并且设置线程名("name")和是否守护线程("isDaemon")等属性的初始化工作
     *
     * @param runnable 该线程要执行的任务
     * @return 包装了Runnable，并完成相关的初始化工作的Thread
     */
    open fun createThread(runnable: Runnable): Thread {
        val thread = Thread(runnable)
        thread.name = nextThreadName()
        thread.isDaemon = daemon
        return thread;
    }

    /**
     * 获取下一个要使用的线程名
     *
     * @return 线程名，依次为"{threadNamePrefix}-1"、"{threadNamePrefix}-2"...
     */
    protected open fun nextThreadName(): String = threadNamePrefix + threadCount.getAndIncrement()
}