package com.wanna.nacos.naming.server.healthcheck

import com.wanna.nacos.naming.server.misc.GlobalExecutor
import com.wanna.common.logging.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 用于去进行健康检测的Reactor
 */
object HealthCheckReactor {

    /**
     * Logger
     */
    @JvmStatic
    private val logger = LoggerFactory.getLogger(HealthCheckReactor::class.java)

    /**
     * 维护的Future的缓存
     */
    @JvmStatic
    private val futureMap = ConcurrentHashMap<String, ScheduledFuture<*>>()

    @JvmStatic
    fun scheduleCheck(healthCheckTask: HealthCheckTask): ScheduledFuture<*> {
        return GlobalExecutor.scheduleNamingHealth(healthCheckTask, 5000L, TimeUnit.MILLISECONDS)
    }

    /**
     * 添加一个实例的健康状态检测的任务给线程池
     *
     * @param task 监控状态检查任务
     */
    @JvmStatic
    fun scheduleCheck(task: ClientBeatCheckTask) {
        futureMap.putIfAbsent(
            task.getTaskKey(),
            GlobalExecutor.scheduleNamingHealth(task, 5000L, 5000L, TimeUnit.MILLISECONDS)
        )
    }

    /**
     * 取消一个客户端的心跳检测
     *
     * @param task 想要取消的ClientBeatCheckTask
     */
    @JvmStatic
    fun cancelCheck(task: ClientBeatCheckTask) {
        val future = futureMap[task.getTaskKey()] ?: return
        try {
            future.cancel(true)
            futureMap.remove(task.getTaskKey())
        } catch (ex: Exception) {
            logger.error("[CANCEL-CHECK] 取消Task失败！", ex)
        }
    }

    /**
     * 立刻去进行指定一个任务
     *
     * @param task 要去执行的任务Runnable
     */
    @JvmStatic
    fun scheduleNow(task: Runnable) {
        GlobalExecutor.scheduleNamingHealth(task, 0, TimeUnit.MILLISECONDS)
    }
}