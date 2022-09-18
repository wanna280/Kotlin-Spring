package com.wanna.framework.scheduling.concurrent

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.context.aware.BeanNameAware
import com.wanna.framework.lang.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor

/**
 * 为juc当中的ExecutorService去提供相关的安装工作的基础类
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
     * ExecutorService
     */
    @Nullable
    private var executorService: ExecutorService? = null

    /**
     * 拒绝策略
     */
    private var rejectedExecutionHandler: RejectedExecutionHandler = ThreadPoolExecutor.AbortPolicy()

    /**
     * ThreadFactory，当前本身就是一个ThreadFactory，因此默认值为this
     *
     * @see CustomizableThreadFactory
     */
    private var threadFactory: ThreadFactory = this

    /**
     * 在初始化Bean时，自动去初始化ExecutorService
     *
     * @see InitializingBean
     */
    override fun afterPropertiesSet() {
        initialize()
    }

    /**
     * 重写父类方法，用于去监控ThreadNamePrefix是否已经完成了初始化工作
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
        // 如果没有设置threadNamePrefix，那么使用beanName去作为ThreadNamePrefix
        if (!this.threadNamePrefixSet && this.beanName != null) {
            setThreadNamePrefix(this.beanName!!)
        }
        this.executorService = initializeExecutor(threadFactory, rejectedExecutionHandler)
    }

    /**
     * 初始化Executor，模板方法，交给具体的子类去进行实现
     *
     * @param threadFactory ThreadFactory(用于创建线程用到的工厂方法)
     * @param rejectedExecutionHandler 线程池的拒绝策略
     */
    protected abstract fun initializeExecutor(
        threadFactory: ThreadFactory,
        rejectedExecutionHandler: RejectedExecutionHandler
    ): ExecutorService

    override fun destroy() {

    }

    override fun setBeanName(beanName: String) {
        this.beanName = beanName
    }
}