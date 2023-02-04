package com.wanna.debugger.bistoury.instrument.client.common

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 为线程池Worker线程指定线程名的[ThreadFactory]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/30
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