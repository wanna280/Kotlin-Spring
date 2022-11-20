package com.wanna.nacos.api.common.task.engine

import com.wanna.nacos.api.common.executor.NameThreadFactory
import com.wanna.nacos.api.common.task.AbstractDelayTask
import com.wanna.nacos.api.common.task.NacosTaskProcessor
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * Nacos的延时任务执行引擎
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 *
 * @param processInterval 执行任务的间隔时间(单位为ms)
 * @param name 执行引擎的name
 */
open class NacosDelayTaskExecuteEngine(name: String, processInterval: Long) {

    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(NacosDelayTaskExecuteEngine::class.java)

    /**
     * Lock
     */
    protected val lock = ReentrantLock()

    /**
     * 维护要去进行执行的延时任务Task列表
     */
    protected val tasks = ConcurrentHashMap<Any, AbstractDelayTask>()

    /**
     * Nacos的TaskProcessor列表
     */
    protected val taskProcessors = ConcurrentHashMap<Any, NacosTaskProcessor>()

    /**
     * 默认的TaskProcessor
     */
    var defaultTaskProcessor: NacosTaskProcessor? = null

    /**
     * 执行任务的线程池
     */
    private var processingExecutor: ScheduledExecutorService

    init {
        processingExecutor = Executors.newSingleThreadScheduledExecutor(NameThreadFactory(name))
        processingExecutor.scheduleWithFixedDelay(
            ProcessRunnable(), processInterval,
            processInterval, TimeUnit.MILLISECONDS
        )
    }

    /**
     * 处理所有的任务
     */
    protected open fun processTasks() {
        val taskKeys = getAllTaskKeys()
        taskKeys.forEach { taskKey ->
            val task = removeTask(taskKey) ?: return@forEach
            val processor = getProcessor(taskKey) ?: return@forEach
            if (!processor.process(task)) {
                retryFailedTask(taskKey, task)
            }
        }
    }

    /**
     * 重试执行失败的任务
     *
     * @param key key
     * @param task task
     */
    private fun retryFailedTask(key: Any, task: AbstractDelayTask) {
        task.lastProcessTime = System.currentTimeMillis()
        addTask(key, task)
    }

    open fun getProcessor(key: Any): NacosTaskProcessor? {
        return taskProcessors[key]
    }

    open fun removeTask(taskKey: Any): AbstractDelayTask? {
        lock.lock()
        try {
            val task = tasks[taskKey]
            return if (task != null && task.shouldProcess()) {
                tasks.remove(taskKey)
            } else {
                null
            }
        } finally {
            lock.unlock()
        }
    }

    /**
     * 获取所有的任务的Key的列表
     *
     * @return taskKey列表
     */
    open fun getAllTaskKeys(): Collection<Any> {
        val taskKeys = LinkedHashSet<Any>()
        lock.lock()
        try {
            taskKeys.addAll(tasks.keys)
        } finally {
            lock.unlock()
        }
        return taskKeys
    }

    open fun addTask(key: Any, task: AbstractDelayTask) {
        lock.lock()
        try {
            tasks[key] = task
        } finally {
            lock.unlock()
        }
    }

    private inner class ProcessRunnable : Runnable {
        override fun run() {
            try {
                processTasks()
            } catch (ex: Exception) {
                logger.error("执行引擎执行任务失败", ex)
            }
        }
    }
}