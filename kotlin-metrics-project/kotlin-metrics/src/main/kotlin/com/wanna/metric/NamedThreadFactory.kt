package com.wanna.metric

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 带名字的[ThreadFactory], 为线程池新创建出来的线程去指定name
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 *
 * @param name 线程名的前缀
 */
open class NamedThreadFactory(private val name: String) : ThreadFactory {

    /**
     * 统计统计当前NamedThreadFactory已经生成过的线程数量, 为线程名的生成提供协助
     */
    private val count = AtomicInteger()

    /**
     * [ThreadFactory]的工厂方法, 去创建线程, 生成threadName, 并设置daemon=true
     *
     * @param r 需要去执行的任务Runnable
     * @return 为该Runnable去创建出来的线程
     */
    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r)
        thread.name = name + "-" + count.getAndIncrement()
        thread.isDaemon = true
        return thread
    }
}