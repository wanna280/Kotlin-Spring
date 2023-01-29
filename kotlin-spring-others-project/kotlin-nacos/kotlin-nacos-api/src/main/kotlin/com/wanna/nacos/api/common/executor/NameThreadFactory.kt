package com.wanna.nacos.api.common.executor

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 支持为线程去指定名字的[ThreadFactory]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
open class NameThreadFactory(name: String) : ThreadFactory {

    /**
     * 如果必要的话, 加上后缀"."
     */
    private val name: String = if (!name.endsWith(".")) "$name." else name

    /**
     * 用于生成线程id的AtomicInteger
     */
    private val id = AtomicInteger()


    /**
     * 根据给定的[Runnable]去创建出来一个[Thread]
     *
     * @return 创建出来的Thread(自定义了线程名, 并且是守护线程)
     */
    override fun newThread(r: Runnable): Thread {
        val threadName = name + id.getAndIncrement()
        val thread = Thread(r, threadName)
        thread.isDaemon = true
        return thread
    }
}