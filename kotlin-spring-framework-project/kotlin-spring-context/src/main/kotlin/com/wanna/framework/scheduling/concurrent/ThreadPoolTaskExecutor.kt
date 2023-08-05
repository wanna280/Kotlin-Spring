package com.wanna.framework.scheduling.concurrent

import com.wanna.framework.core.task.AsyncListenableTaskExecutor
import com.wanna.framework.core.task.AsyncTaskExecutor
import com.wanna.framework.core.task.TaskDecorator
import com.wanna.framework.core.task.TaskRejectedException
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.concurrent.ListenableFuture
import java.util.concurrent.*
import kotlin.jvm.Throws

/**
 * Spring家的任务执行器, 主要对应于juc家的ThreadPoolExecutor, 通过组合一个ThreadPoolExecutor完成实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/18
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see ExecutorService
 */
open class ThreadPoolTaskExecutor : ExecutorConfigurationSupport(), AsyncListenableTaskExecutor {

    /**
     * 操作poolSize的锁, 因为对于ThreadPoolExecutor而言, corePoolSize、maxPoolSize和keepAliveSeconds
     * 这三个参数都是可以去进行后续的修改的, 因此需要对后期的修改去进行加锁保证并发安全
     *
     * @see corePoolSize
     * @see maxPoolSize
     * @see keepAliveSeconds
     */
    private val poolSizeMonitor = Any()

    /**
     * 核心线程数量, 默认为1个核心线程
     */
    private var corePoolSize = 1

    /**
     * 最大线程数量, 默认为Int.MAX_VALUE
     */
    private var maxPoolSize = Int.MAX_VALUE

    /**
     * 阻塞队列的容量
     */
    private var queueCapacity = Int.MAX_VALUE

    /**
     * 非核心线程数的空闲时间, 单位为s
     */
    private var keepAliveSeconds = 60

    /**
     * 是否允许核心线程超时? 默认为false
     */
    private var allowCoreThreadTimeOut = false

    /**
     * 是否需要预先启动所有的核心线程? 默认为false
     */
    private var prestartAllCoreThreads = false

    /**
     * TaskDecorator, 用于对提交给线程池的任务去进行自定义的装饰
     *
     * @see TaskDecorator
     */
    private var taskDecorator: TaskDecorator? = null

    /**
     * ThreadPoolExecutor
     */
    @Nullable
    private var threadPoolExecutor: ThreadPoolExecutor? = null

    /**
     * 被TaskDecorator所装饰过的Runnable列表, key是原始的, value是被包装之后的
     */
    private var decoratedTaskMap = ConcurrentHashMap<Runnable, Any>()

    /**
     * 获取ThreadPoolExecutor
     *
     * @return ThreadPoolExecutor
     * @throws IllegalStateException 如果ThreadPoolExecutor没有被初始化的话
     */
    @Throws(IllegalStateException::class)
    open fun getThreadPoolExecutor(): ThreadPoolExecutor =
        threadPoolExecutor ?: throw IllegalStateException("ThreadPoolExecutor不能为空")

    /**
     * 初始化Executor, 我们这里需要初始化的是一个ThreadPoolExecutor
     *
     * @param threadFactory ThreadFactory, 用于线程池的Worker线程的创建的工厂方法
     * @param rejectedExecutionHandler 线程池的拒绝策略
     */
    override fun initializeExecutor(
        threadFactory: ThreadFactory,
        rejectedExecutionHandler: RejectedExecutionHandler
    ): ExecutorService {
        val queue = createQueue(queueCapacity)
        val executor: ThreadPoolExecutor

        if (taskDecorator != null) {
            executor = object : ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveSeconds.toLong(),
                TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler
            ) {
                override fun execute(command: Runnable) {
                    val decorated = taskDecorator?.decorate(command) ?: throw IllegalStateException("TaskDecorator不能为空")
                    if (decorated != command) {
                        decoratedTaskMap[command] = decorated
                    }
                    super.execute(decorated)  // super.execute
                }
            }
        } else {
            executor = ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveSeconds.toLong(),
                TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler
            )
        }
        if (allowCoreThreadTimeOut) {
            executor.allowCoreThreadTimeOut(true)
        }
        if (prestartAllCoreThreads) {
            executor.prestartAllCoreThreads()
        }
        this.threadPoolExecutor = executor
        return executor
    }

    @Throws(TaskRejectedException::class)
    override fun submit(task: Runnable): Future<*> {
        try {
            return getThreadPoolExecutor().submit(task)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getThreadPoolExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    override fun <T> submit(task: Callable<T>): Future<T> {
        try {
            return getThreadPoolExecutor().submit(task)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getThreadPoolExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    override fun execute(task: Runnable) {
        try {
            getThreadPoolExecutor().execute(task)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getThreadPoolExecutor()}]所拒绝掉", ex)
        }
    }

    override fun submitListenable(task: Runnable): ListenableFuture<*> {
        TODO("Not yet implemented")
    }

    override fun <T> submitListenable(task: Callable<T>): ListenableFuture<T> {
        TODO("Not yet implemented")
    }

    open fun setTaskDecorator(taskDecorator: TaskDecorator?) {
        this.taskDecorator = taskDecorator
    }

    open fun getTaskDecorator(): TaskDecorator? = this.taskDecorator

    open fun setCorePoolSize(corePoolSize: Int) {
        synchronized(poolSizeMonitor) {
            this.threadPoolExecutor?.corePoolSize = corePoolSize
            this.corePoolSize = corePoolSize
        }
    }

    open fun getCorePoolSize(): Int {
        synchronized(poolSizeMonitor) {
            return this.corePoolSize
        }
    }

    open fun setMaxPoolSize(maxPoolSize: Int) {
        synchronized(poolSizeMonitor) {
            this.threadPoolExecutor?.maximumPoolSize = maxPoolSize
            this.maxPoolSize = maxPoolSize
        }
    }

    open fun getMaxPoolSize(): Int {
        synchronized(poolSizeMonitor) {
            return this.maxPoolSize
        }
    }

    open fun setKeepAliveSeconds(keepAliveSeconds: Int) {
        synchronized(poolSizeMonitor) {
            threadPoolExecutor?.setKeepAliveTime(keepAliveSeconds.toLong(), TimeUnit.SECONDS)
            this.keepAliveSeconds = keepAliveSeconds
        }
    }

    open fun getKeepAliveSeconds(): Int {
        synchronized(poolSizeMonitor) {
            return keepAliveSeconds
        }
    }

    open fun setQueueCapacity(queueCapacity: Int) {
        this.queueCapacity = queueCapacity
    }

    open fun getQueueCapacity(): Int = this.queueCapacity

    open fun setAllowCoreThreadTimeOut(allowCoreThreadTimeOut: Boolean) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut
    }

    open fun isAllowCoreThreadTimeOut(): Boolean = this.allowCoreThreadTimeOut

    open fun setPrestartAllCoreThreads(prestartAllCoreThreads: Boolean) {
        this.prestartAllCoreThreads = prestartAllCoreThreads
    }

    open fun isPrestartAllCoreThreads(): Boolean = this.prestartAllCoreThreads

    /**
     * 创建用于线程池的BlockingQueue
     *
     * @param capacity 阻塞队列的最大容量
     * @return 创建好的阻塞队列
     */
    protected open fun createQueue(capacity: Int): BlockingQueue<Runnable> =
        if (capacity > 0) LinkedBlockingQueue(capacity) else SynchronousQueue()
}