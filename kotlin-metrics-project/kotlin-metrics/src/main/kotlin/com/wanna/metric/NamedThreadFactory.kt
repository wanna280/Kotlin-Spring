package com.wanna.metric

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 带名字的[ThreadFactory], 为线程池新创建出来的线程去指定name
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 */
open class NamedThreadFactory(private val name: String) : ThreadFactory {

    private val count = AtomicInteger()

    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r)
        thread.name = name + "-" + count.getAndIncrement()
        thread.isDaemon = true
        return thread
    }
}