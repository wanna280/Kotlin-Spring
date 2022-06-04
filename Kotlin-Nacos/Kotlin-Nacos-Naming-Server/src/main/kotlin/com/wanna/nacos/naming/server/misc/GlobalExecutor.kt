package com.wanna.nacos.naming.server.misc

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 全局线程池
 */
object GlobalExecutor {

    // 用于Naming的心跳检测的线程池
    private val NAMING_HEALTH_EXECUTOR = ScheduledThreadPoolExecutor(2)

    @JvmStatic
    fun scheduleNamingHealth(command: Runnable, initDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
        return NAMING_HEALTH_EXECUTOR.scheduleWithFixedDelay(command, initDelay, delay, unit)
    }

    @JvmStatic
    fun scheduleNamingHealth(command: Runnable, delay: Long, unit: TimeUnit) : ScheduledFuture<*> {
        return NAMING_HEALTH_EXECUTOR.schedule(command, delay, unit)
    }
}