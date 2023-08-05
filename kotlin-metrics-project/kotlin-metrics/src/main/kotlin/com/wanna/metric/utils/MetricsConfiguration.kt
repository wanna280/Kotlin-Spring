package com.wanna.metric.utils

import com.wanna.metric.server.MetricServer
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Metrics的配置信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 */
object MetricsConfiguration {

    /**
     * Logger
     */
    @JvmStatic
    private val logger = LoggerFactory.getLogger(MetricsConfiguration::class.java)

    /**
     * Metrics的配置文件的位置
     */
    private const val CONFIG_FILE_PATH = "metrics.properties"

    /**
     * [MetricServer]暴露服务启动的端口号的Key
     */
    private const val METRIC_SERVER_PORT_KEY = "metric.server.port"

    /**
     * 异步计算任务线程池的核心线程数量的Key
     */
    private const val METRIC_CORE_CALCULATE_THREADS_KEY = "metric.calculate.threads.core"

    /**
     * 异步计算任务线程池的最大线程数量的Key
     */
    private const val METRIC_MAX_CALCULATE_THREADS_KEY = "metric.calculate.thread.max"

    /**
     * 异步计算任务线程池的阻塞队列大小
     */
    private const val METRIC_CALCULATE_QUEUE_SIZE_KEY = "metric.calculate.queue.size"

    /**
     * 异步计算任务线程池的核心线程数
     */
    var coreCalculateThreads = 2
        internal set

    /**
     * 异步计算任务线程池的最大线程数
     */
    var maxCalculateThreads = 2
        internal set

    /**
     * 异步计算任务线程池的队列最大容量
     */
    var maxCalculateQueueSize = 1000000
        internal set

    /**
     * [MetricServer]要去进行暴露的端口
     */
    var metricServerPort = 7777
        internal set

    init {
        logger.info("metric-config: try to load $CONFIG_FILE_PATH...")
        val configFileStream = MetricsConfiguration.javaClass.classLoader.getResourceAsStream(CONFIG_FILE_PATH)
        if (configFileStream != null) {

            // 如果存在有配置文件, 那么将会使用给定的配置文件去作为配置信息
            val properties = Properties()
            properties.load(configFileStream)

            // 尝试读取MetricServer启动端口号
            try {
                this.metricServerPort =
                    properties.getOrDefault(METRIC_SERVER_PORT_KEY, metricServerPort).toString().toInt()
                logger.info("metric-config: use {} as metric-server", metricServerPort)
            } catch (ex: Exception) {
                logger.error("metric-config: read metric-server port failed...", ex)
            }

            // 尝试读取异步计算任务的核心线程数量
            try {
                this.coreCalculateThreads =
                    properties.getOrDefault(METRIC_CORE_CALCULATE_THREADS_KEY, coreCalculateThreads).toString().toInt()
                logger.info("metric-config: use {} as core calculate threads...", this.coreCalculateThreads)
            } catch (ex: Exception) {
                logger.error("metric-config: read core calculate threads failed...", ex)
            }

            // 尝试读取异步计算任务的最大线程数量
            try {
                this.maxCalculateThreads =
                    properties.getOrDefault(METRIC_MAX_CALCULATE_THREADS_KEY, maxCalculateThreads).toString().toInt()
                logger.info("metric-config: use {} as max calculate threads...", this.maxCalculateThreads)
            } catch (ex: Exception) {
                logger.error("metric-config: read max calculate threads failed...", ex)
            }

            // 尝试读取异步计算任务的阻塞队列数量
            try {
                this.maxCalculateQueueSize =
                    properties.getOrDefault(METRIC_CALCULATE_QUEUE_SIZE_KEY, maxCalculateQueueSize).toString().toInt()
                logger.info("metric-config: use {} as calculate queue size....", this.maxCalculateQueueSize)
            } catch (ex: Exception) {
                logger.error("metric-config: read calculate queue size failed...", ex)
            }
        } else {
            // 如果没有指定配置文件, 将会使用所有的默认值...
            logger.info("metric-config: cannot load $CONFIG_FILE_PATH, try all default configurations...")
        }
    }
}