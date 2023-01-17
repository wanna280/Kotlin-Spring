package com.wanna.metric.utils

/**
 * Metrics的配置信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 */
object MetricsConfiguration {

    /**
     * 异步计算任务线程池的核心线程数
     */
    var coreCalculateThreads = 2

    /**
     * 异步计算任务线程池的最大线程数
     */
    var maxCalculateThreads = 2

    /**
     * 异步计算任务线程池的队列最大容量
     */
    var maxCalculateQueueSize = 1000000
}