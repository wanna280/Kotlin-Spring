package com.wanna.framework.util

import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * 支持去进行自定义的ThreadCreator，提供`createThread`方法去提供线程的创建的工厂方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/19
 * @see createThread
 */
open class CustomizableThreadCreator : Serializable {

    /**
     * ThreadNamePrefix
     */
    private var threadNamePrefix = this.getDefaultThreadNamePrefix()

    /**
     * 线程优先级
     */
    private var threadPriority = 5

    /**
     * 要去进行创建的线程是否是守护线程？
     */
    private var daemon = false

    /**
     * ThreadGroup
     */
    private var threadGroup: ThreadGroup? = null


    /**
     * 维护了线程的数量，使用AtomicInteger去生成线程名的后缀
     */
    private val threadCount = AtomicInteger()

    open fun setThreadNamePrefix(threadNamePrefix: String) {
        this.threadNamePrefix = threadNamePrefix
    }

    open fun getThreadNamePrefix(): String = this.threadNamePrefix

    open fun setThreadPriority(threadPriority: Int) {
        this.threadPriority = threadPriority
    }

    open fun getThreadPriority(): Int = this.threadPriority

    open fun setDaemon(daemon: Boolean) {
        this.daemon = daemon
    }

    open fun isDaemon(): Boolean = this.daemon

    open fun setThreadGroup(threadGroup: ThreadGroup?) {
        this.threadGroup = threadGroup
    }

    open fun getThreadGroup(): ThreadGroup? = this.threadGroup

    /**
     * 根据给定的Runnable去创建一个线程
     *
     * @return 创建好的Thread线程对象
     */
    open fun createThread(runnable: Runnable): Thread {
        val thread = Thread(threadGroup, runnable, nextThreadName())
        thread.isDaemon = daemon
        thread.priority = threadPriority
        return thread
    }

    /**
     * 获取下一个要去进行创建的线程名
     *
     * @return 下一个即将要去进行使用的ThreadName
     */
    protected open fun nextThreadName(): String = threadNamePrefix + threadCount.getAndIncrement()

    /**
     * 获取默认的ThreadNamePrefix，采用类名+"-"的方式去进行生成
     *
     * @return ThreadNamePrefix
     */
    protected open fun getDefaultThreadNamePrefix(): String = ClassUtils.getShortName(javaClass.name) + "-"
}