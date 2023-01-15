package com.wanna.framework.scheduling.concurrent

import com.wanna.framework.core.task.AsyncListenableTaskExecutor
import com.wanna.framework.core.task.TaskRejectedException
import com.wanna.framework.lang.Nullable
import com.wanna.framework.scheduling.TaskScheduler
import com.wanna.framework.util.concurrent.ListenableFuture
import com.wanna.framework.util.concurrent.ListenableFutureCallback
import com.wanna.framework.util.concurrent.ListenableFutureTask
import java.time.Clock
import java.util.*
import java.util.concurrent.*
import kotlin.jvm.Throws

/**
 * Spring家实现的ThreadPool的TaskScheduler, 用于去进行定时任务的调度,
 * 它的内部通过包装一个juc当中提供的ScheduledExecutorService去完成最终的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/20
 * @see ScheduledExecutorService
 * @see ScheduledThreadPoolExecutor
 * @see ExecutorConfigurationSupport
 * @see AsyncListenableTaskExecutor
 */
open class ThreadPoolTaskScheduler : ExecutorConfigurationSupport(), TaskScheduler, AsyncListenableTaskExecutor {

    /**
     * ScheduledExecutorService线程池的核心线程数量, 默认为1
     */
    @Volatile
    private var corePoolSize = 1

    /**
     * 在一个任务被取消时, 是否应该将该任务从阻塞队列当中去进行移除掉？
     * 默认为false; 之所以存在有这样的一个标志位, 是在于如果不去进行移除的话,
     * 那么工作队列当中就维护了已经取消的任务列表和还没被取消的任务的视图, 这个
     * 视图有可能是需要的, 因此设计了这么一个标志位, 允许不从阻塞队列当中移除;
     * 另外一方面, 移除操作时需要加锁, 会影响别的线程从队列当中去获取任务去进行执行
     *
     * ```kotlin
     *    ScheduledFuture<*> future = ScheduleExecutorService.scheduleAtFixDelay(...)
     *    future.cancel(true)
     * ```
     */
    @Volatile
    private var removeOnCancelPolicy = false

    /**
     * 当线程池关闭时, 是否应该继续执行那些周期性任务？默认为false
     *
     * 主要针对的是scheduleWithFixedDelay和scheduleAtFixRate两个方法丢给线程池去进行执行的任务,
     * 在线程池关闭时, 我们是否还需要继续执行该任务？默认情况下为false, 当线程池关闭时, 就不再去执行该任务了
     */
    @Volatile
    private var continueExistingPeriodicTasksAfterShutdownPolicy = false

    /**
     * 当线程池关闭时, 是否还应该执行已经延时的任务？默认为true
     *
     * 主要针对的是使用schedule方法丢给线程池的任务, 有可能丢给线程池了,
     * 但是该任务还没执行完, 或者是甚至还没有被执行到, 因此我们很可能是
     * 需要保证该任务一定被执行的, 所以默认值也被设置成为true
     */
    @Volatile
    private var executeExistingDelayedTasksAfterShutdownPolicy = true

    /**
     * 获取一个默认时区的钟表, 用于获取当前时间, 在使用它的{@code millis()}方法时,
     * 差不多可以相当于使用{@code System.currentTimeMillis()}
     *
     * @see System.currentTimeMillis
     */
    private var clock = Clock.systemDefaultZone()

    /**
     * ScheduledExecutorService, 真正执行定时任务的线程池
     */
    @Nullable
    private var scheduledExecutorService: ScheduledExecutorService? = null

    /**
     * ScheduledFutureTask-->ListenableFuture的Map
     */
    private val listenableFutureMap = ConcurrentHashMap<Any, ListenableFuture<*>>()

    /**
     * 设置ScheduledExecutorService的核心线程数量, 默认为1个核心线程
     *
     * @param corePoolSize 执行定时任务的线程池的核心线程数量
     */
    open fun setCorePoolSize(corePoolSize: Int) {
        if (scheduledExecutorService is ScheduledThreadPoolExecutor) {
            (scheduledExecutorService as ScheduledThreadPoolExecutor).corePoolSize = corePoolSize
        }
        this.corePoolSize = corePoolSize
    }

    open fun getCorePoolSize(): Int = this.corePoolSize

    /**
     * 获取ScheduledExecutorService的线程池的Worker数量
     *
     * @return poolSize
     */
    open fun getPoolSize(): Int =
        if (scheduledExecutorService == null) this.corePoolSize else getScheduledThreadPoolExecutor().poolSize

    /**
     * 在取消任务时, 是否应该将该任务从阻塞队列当中去进行移除掉？默认为false
     *
     * @param removeOnCancelPolicy 如果为true, 那么取消任务时, 也会从阻塞队列当中去进行移除
     */
    open fun setRemoveOnCancelPolicy(removeOnCancelPolicy: Boolean) {
        (this.scheduledExecutorService as? ScheduledThreadPoolExecutor)?.removeOnCancelPolicy = removeOnCancelPolicy
        this.removeOnCancelPolicy = removeOnCancelPolicy
    }

    /**
     * 在线程池关闭时, 是否还应该继续执行周期性的任务？默认为false
     *
     * @param flag 如果为true, 在线程池关闭时, 还应该继续执行周期性任务
     */
    open fun setContinueExistingPeriodicTasksAfterShutdownPolicy(flag: Boolean) {
        (this.scheduledExecutorService as? ScheduledThreadPoolExecutor)?.continueExistingPeriodicTasksAfterShutdownPolicy =
            flag
        this.continueExistingPeriodicTasksAfterShutdownPolicy = flag
    }

    /**
     * 在线程池关闭时, 是否还应该继续执行已经被延时的任务？默认为true
     *
     * @param flag 如果为true, 在线程池关闭时, 还应该继续执行已经被延时的任务
     */
    open fun setExecuteExistingDelayedTasksAfterShutdownPolicy(flag: Boolean) {
        (this.scheduledExecutorService as? ScheduledThreadPoolExecutor)?.executeExistingDelayedTasksAfterShutdownPolicy =
            flag
        this.executeExistingDelayedTasksAfterShutdownPolicy = flag
    }

    open fun setClock(clock: Clock) {
        this.clock = clock
    }

    open fun getClock(): Clock = this.clock

    /**
     * 获取ScheduledExecutor
     *
     * @return ScheduledExecutorService
     * @throws IllegalStateException 如果内部组合的ScheduledExecutorService为空的话
     */
    @Throws(IllegalStateException::class)
    open fun getScheduledExecutor(): ScheduledExecutorService =
        this.scheduledExecutorService ?: throw IllegalStateException("ScheduledExecutorService不能为null")

    /**
     * 获取ScheduledThreadPoolExecutor
     *
     * @return 将ScheduledExecutorService强转成为ScheduledThreadPoolExecutor
     * @throws IllegalStateException 如果内部组合的ScheduledExecutorService为空, 或者它的类型并不是ScheduledThreadPoolExecutor
     */
    @Throws(IllegalStateException::class)
    open fun getScheduledThreadPoolExecutor(): ScheduledThreadPoolExecutor =
        (this.scheduledExecutorService as? ScheduledThreadPoolExecutor)
            ?: throw IllegalStateException("ScheduledExecutorService必须为ScheduledThreadPoolExecutor类型")

    //---------------------------AsyncListenableTaskExecutor Implementations---------------------------------------

    @Throws(TaskRejectedException::class)
    override fun execute(task: Runnable) {
        try {
            getScheduledExecutor().execute(task)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getScheduledExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    override fun submit(task: Runnable): Future<*> {
        try {
            return getScheduledExecutor().submit(task)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getScheduledExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    override fun <T> submit(task: Callable<T>): Future<T> {
        try {
            return getScheduledExecutor().submit(task)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getScheduledExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    override fun submitListenable(task: Runnable): ListenableFuture<*> {
        try {
            val listenableFutureTask = ListenableFutureTask<Any> { task.run() }
            executeAndTrack(getScheduledExecutor(), listenableFutureTask)
            return listenableFutureTask
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getScheduledExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    override fun <T> submitListenable(task: Callable<T>): ListenableFuture<T> {
        try {
            val listenableFutureTask = ListenableFutureTask<T>(task)
            executeAndTrack(getScheduledExecutor(), listenableFutureTask)
            return listenableFutureTask
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getScheduledExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    private fun executeAndTrack(executor: ExecutorService, listenableFuture: ListenableFutureTask<*>) {
        val scheduledFuture = executor.submit(listenableFuture)
        listenableFutureMap[scheduledFuture] = listenableFuture
        @Suppress("UNCHECKED_CAST")
        (listenableFuture as ListenableFuture<Any>).addCallback(object : ListenableFutureCallback<Any> {
            override fun onError(ex: Throwable) {
                listenableFutureMap.remove(scheduledFuture)
            }

            override fun onSuccess(result: Any?) {
                listenableFutureMap.remove(scheduledFuture)
            }
        })
    }

    //---------------------------TaskScheduler Implementations---------------------------------------

    @Throws(TaskRejectedException::class)
    override fun scheduleWithFixedDelay(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*> {
        try {
            val initDelay = startTime.time - clock.millis()
            return getScheduledExecutor().scheduleWithFixedDelay(task, initDelay, delay, TimeUnit.MILLISECONDS)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getScheduledExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    override fun scheduleWithFixedDelay(task: Runnable, delay: Long): ScheduledFuture<*> {
        try {
            return getScheduledExecutor().scheduleWithFixedDelay(task, 0L, delay, TimeUnit.MILLISECONDS)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getScheduledExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    override fun scheduleAtFixedRate(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*> {
        try {
            val initDelay = startTime.time - clock.millis()
            return getScheduledExecutor().scheduleAtFixedRate(task, initDelay, delay, TimeUnit.MILLISECONDS)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getScheduledExecutor()}]所拒绝掉", ex)
        }
    }

    @Throws(TaskRejectedException::class)
    override fun scheduleAtFixedRate(task: Runnable, delay: Long): ScheduledFuture<*> {
        try {
            return getScheduledExecutor().scheduleAtFixedRate(task, 0, delay, TimeUnit.MILLISECONDS)
        } catch (ex: RejectedExecutionException) {
            throw TaskRejectedException("任务${task}被线程池[${getScheduledExecutor()}]所拒绝掉", ex)
        }
    }

    /**
     * 初始化Executor, 我们这里需要初始化的ScheduledThreadPoolExecutor
     *
     * @param threadFactory ThreadFactory
     * @param rejectedExecutionHandler 线程池的拒绝策略处理器
     */
    override fun initializeExecutor(
        threadFactory: ThreadFactory,
        rejectedExecutionHandler: RejectedExecutionHandler
    ): ExecutorService {
        val executor = createExecutor(this.corePoolSize, threadFactory, rejectedExecutionHandler)
        // 更新定时任务线程池的相关策略
        if (executor is ScheduledThreadPoolExecutor) {
            executor.continueExistingPeriodicTasksAfterShutdownPolicy = continueExistingPeriodicTasksAfterShutdownPolicy
            executor.executeExistingDelayedTasksAfterShutdownPolicy = executeExistingDelayedTasksAfterShutdownPolicy
            executor.removeOnCancelPolicy = removeOnCancelPolicy
        }
        this.scheduledExecutorService = executor
        return executor
    }

    /**
     * 执行真正的创建ScheduledExecutorService的方法, 作为模板方法, 允许子类去进行自定义
     *
     * @param corePoolSize 线程池的核心线程数量
     * @param threadFactory 用于创建线程池的Worker线程的ThreadFactory
     * @param rejectedExecutionHandler 线程池的拒绝策略
     * @return 构建出来的ScheduledExecutorService
     */
    protected open fun createExecutor(
        corePoolSize: Int,
        threadFactory: ThreadFactory,
        rejectedExecutionHandler: RejectedExecutionHandler
    ): ScheduledExecutorService {
        return ScheduledThreadPoolExecutor(corePoolSize, threadFactory, rejectedExecutionHandler)
    }
}