package com.wanna.nacos.naming.server.misc

import com.wanna.nacos.api.common.executor.NameThreadFactory
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 维护全局当中做各种事情的线程池, 做资源隔离
 */
object GlobalExecutor {

    /**
     * 用于Naming的心跳检测的线程池
     */
    @JvmStatic
    private val NAMING_HEALTH_EXECUTOR =
        ScheduledThreadPoolExecutor(2, NameThreadFactory("com.wanna.nacos.naming.health-check.notifier"))

    @JvmStatic
    fun scheduleNamingHealth(command: Runnable, initDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
        return NAMING_HEALTH_EXECUTOR.scheduleWithFixedDelay(command, initDelay, delay, unit)
    }

    @JvmStatic
    fun scheduleNamingHealth(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
        return NAMING_HEALTH_EXECUTOR.schedule(command, delay, unit)
    }
}