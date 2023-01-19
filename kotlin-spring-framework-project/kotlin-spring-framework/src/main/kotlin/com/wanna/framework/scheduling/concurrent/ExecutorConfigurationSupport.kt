package com.wanna.framework.scheduling.concurrent

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.context.aware.BeanNameAware
import com.wanna.framework.lang.Nullable
import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import java.util.concurrent.*

/**
 * 为juc当中的ExecutorService去提供相关的安装工作的基础类,
 * 它的子类当中是为整个Spring当中的并发相关的Executor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/18
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledExecutorService
 * @see java.util.concurrent.Executors
 */
abstract class ExecutorConfigurationSupport : InitializingBean, BeanNameAware, DisposableBean,
    CustomizableThreadFactory() {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    /**
     * 是否已经设置过了ThreadNamePrefix的标志位
     */
    private var threadNamePrefixSet: Boolean = false

    /**
     * beanName
     */
    @Nullable
    private var beanName: String? = null

    /**
     * 在线程池关闭时, 我们是否需要等待任务执行完成? 默认为false
     */
    private var waitForTasksToCompleteOnShutdown = false

    /**
     * 需要等待线程池处理结束工作的时间(单位为ms), 默认为0L
     */
    private var awaitTerminationMillis = 0L

    /**
     * ExecutorService
     */
    @Nullable
    private var executorService: ExecutorService? = null

    /**
     * 拒绝策略
     */
    private var rejectedExecutionHandler: RejectedExecutionHandler = ThreadPoolExecutor.AbortPolicy()

    /**
     * ThreadFactory, 当前本身就是一个ThreadFactory, 因此默认值为this
     *
     * @see CustomizableThreadFactory
     */
    private var threadFactory: ThreadFactory = this

    /**
     * 在初始化Bean时, 自动去初始化ExecutorService
     *
     * @see InitializingBean
     */
    override fun afterPropertiesSet() {
        initialize()
    }

    /**
     * 重写父类方法, 用于去监控ThreadNamePrefix是否已经完成了初始化工作
     *
     * @param threadNamePrefix ThreadNamePrefix
     */
    override fun setThreadNamePrefix(threadNamePrefix: String) {
        super.setThreadNamePrefix(threadNamePrefix)
        this.threadNamePrefixSet = true
    }

    open fun initialize() {
        if (logger.isDebugEnabled) {
            logger.debug("正在初始化ExecutorService")
        }
        // 如果没有设置threadNamePrefix, 那么使用beanName去作为ThreadNamePrefix
        if (!this.threadNamePrefixSet && this.beanName != null) {
            setThreadNamePrefix(this.beanName!!)
        }
        this.executorService = initializeExecutor(threadFactory, rejectedExecutionHandler)
    }

    /**
     * 初始化Executor, 模板方法, 交给具体的子类去进行实现
     *
     * @param threadFactory ThreadFactory(用于创建线程用到的工厂方法)
     * @param rejectedExecutionHandler 线程池的拒绝策略
     */
    protected abstract fun initializeExecutor(
        threadFactory: ThreadFactory,
        rejectedExecutionHandler: RejectedExecutionHandler
    ): ExecutorService

    /**
     * 在当前的Bean摧毁时, 自动对线程池去进行关闭
     *
     * @see ExecutorService.shutdown
     * @see ExecutorService.shutdownNow
     */
    override fun destroy() {
        shutdown()
    }

    open fun shutdown() {
        if (logger.isDebugEnabled) {
            logger.debug("正在关闭ExecutorService线程池[${this.beanName ?: ""}]")
        }
        val executor = this.executorService
        if (executor != null) {
            if (this.waitForTasksToCompleteOnShutdown) {
                executor.shutdown()
            } else {
                // shutdownNow, 并取消掉线程池当中的所有的剩余的任务
                executor.shutdownNow().forEach(this::cancelRemainingTask)
                // 如果必要的话, 等一会线程池去处理结束的收尾工作才结束...
                awaitTerminationIfNecessary(executor)
            }
        }
    }

    /**
     * 在立刻去关闭线程池时, 需要去处理剩下的任务列表
     *
     * @param task 关闭线程池剩下的任务
     */
    protected open fun cancelRemainingTask(task: Runnable) {
        if (task is Future<*>) {
            task.cancel(true)
        }
    }

    private fun awaitTerminationIfNecessary(executor: ExecutorService) {
        if (this.awaitTerminationMillis > 0) {
            try {
                if (!executor.awaitTermination(awaitTerminationMillis, TimeUnit.MILLISECONDS)) {
                    if (logger.isWarnEnabled) {
                        logger.warn("在等待线程池[${beanName ?: ""}]执行terminate的过程当中, 出现了超时的情况")
                    }
                }
            } catch (ex: InterruptedException) {
                if (logger.isWarnEnabled) {
                    logger.warn("在等待线程池[${beanName ?: ""}]执行terminate的过程当中, 遇到了线程被中断的情况")
                }
                Thread.currentThread().interrupt()  // self interrupt
            }
        }
    }

    override fun setBeanName(beanName: String) {
        this.beanName = beanName
    }
}