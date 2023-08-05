package com.wanna.metric

import com.wanna.metric.utils.MetricsConfiguration
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 定时去汇总监控指标的定时任务
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 */
object MetricScheduleTask {
    /**
     * Logger
     */
    @JvmStatic
    private val logger = LoggerFactory.getLogger(MetricScheduleTask::class.java)

    /**
     * Count后缀
     */
    private const val COUNT_SUFFIX = "_Count"

    /**
     * Time后缀
     */
    private const val TIME_SUFFIX = "_Time"

    /**
     * Value后缀
     */
    private const val VALUE_SUFFIX = "_Value"

    /**
     * JVM前缀
     */
    private const val JVM_PREFIX = "JVM_"

    /**
     * 空字符串常量
     */
    private const val EMPTY = ""


    /**
     * 执行定时汇报指标的线程池, 每2秒尝试去执行一次, 将指标汇总到Metrics当中
     *
     * @see Metrics.currentItems
     * @see Metrics.currentSettingItems
     */
    @JvmStatic
    private val metricScheduleExecutor = ScheduledThreadPoolExecutor(1, NamedThreadFactory("metric-schedule"))

    /**
     * 执行异步任务的计算的线程池
     */
    @JvmStatic
    private val metricAsyncCalculateExecutor = ThreadPoolExecutor(
        MetricsConfiguration.coreCalculateThreads,
        MetricsConfiguration.maxCalculateThreads,
        60L,
        TimeUnit.SECONDS,
        LinkedBlockingDeque(MetricsConfiguration.maxCalculateQueueSize),
        NamedThreadFactory("metric-async-calculate")
    ) { _, _ ->
        logger.error("metric-schedule: add async calculate task error")
    }


    /**
     * 启动相关定时任务, 去进行监控指标的快照记录
     */
    @JvmStatic
    fun loadSchedule() {
        // log init start
        logger.info("metric-schedule: init start...")
        // 添加一个指标快照的收集的MetricTask定时任务
        metricScheduleExecutor.scheduleAtFixedRate(MetricTask(), 0, 2000L, TimeUnit.MILLISECONDS)

        // log success
        logger.info("metric-schedule: init successfully...")


        // 添加ShutdownHook, 当应用关闭时, 自动关闭线程池...
        Runtime.getRuntime().addShutdownHook(Thread({
            logger.info("metric-schedule: executor shutdown")
            metricScheduleExecutor.shutdown()

            logger.info("metric-schedule: async calculate executor shutdown")
            metricAsyncCalculateExecutor.shutdownNow()
        }, "metrics-shutdown-hook-thread"))
        logger.info("metric-init finished...")
    }

    /**
     * 交给执行异步任务线程池去执行一个任务
     *
     * @param task 要交给线程池去进行执行的Runnable任务
     * @see metricAsyncCalculateExecutor
     */
    @JvmStatic
    fun execute(task: Runnable) {
        metricAsyncCalculateExecutor.execute(task)
    }

    /**
     * MetricTask定时任务, 负责去将指标汇总到Metrics当中
     *
     * @see Metrics.currentItems
     * @see Metrics.currentSettingItems
     */
    private class MetricTask : Runnable {

        /**
         * 上次更新的时间戳
         */
        private var lastUpdate = 0L

        override fun run() {
            val current = System.currentTimeMillis()
            // 如果距离上次处理的间隔时间低于50s, 那么pass...
            if (current - lastUpdate < 50 * 1000L) {
                return
            }
            // 如果当前时间对应的秒大于10, 那么pass, 只在每分钟的0-9秒才能去汇总数据...
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = current
            if (calendar.get(Calendar.SECOND) > 10) {
                return
            }

            lastUpdate = current

            val metrics = LinkedHashMap<String, Any>()
            val settingMetrics = LinkedHashMap<String, Any>()

            // 将JVM的监控指标信息汇总到Metrics当中...
            metricsJVM(metrics)

            // 将Tomcat的监控指标信息汇总到Metrics当中...
            metricsTomcat(metrics)

            // 将用户自定义的count/time类型的Metric指标去汇总到Metrics当中...
            for (avgItem in Metrics.avgItems) {

                makeMetricResult(metrics, settingMetrics, avgItem.key, avgItem.value.dumpAndClearItem())
            }

            // 将values去copy一份, 并且clear掉Metrics当中保存的结果, 重新去进行统计...
            val valuesMetrics = LinkedHashMap(Metrics.values)
            Metrics.values.clear()

            // 对values类型的的监控指标去进行收集
            for (valuesMetric in valuesMetrics) {
                val valueMetricName = makeMetricName(EMPTY, valuesMetric.key, VALUE_SUFFIX)

                // 记录一个Value类型的数量指标
                metrics[valueMetricName] = valuesMetric.value.get()
            }

            // 在最后, 再去计算一下定时任务的耗时时间为多少?

            metrics[makeMetricName("Metric", "Schedule_Task", TIME_SUFFIX)] = System.currentTimeMillis() - current


            // 将汇总得到的快照指标数据去保存到Metrics当中去
            Metrics.currentSettingItems = Collections.unmodifiableMap(settingMetrics)
            Metrics.currentItems = Collections.unmodifiableMap(metrics)
        }

        /**
         * 生成指标名
         *
         * @param prefix 指标名前缀
         * @param name 指标名
         * @param suffix 指标名后缀(例如"_Count"/"_Time")
         */
        private fun makeMetricName(prefix: String, name: String, suffix: String): String {
            return prefix + name + suffix
        }

        private fun makeMetricResult(
            metrics: MutableMap<String, Any>,
            settingMetrics: MutableMap<String, Any>,
            name: String,
            item: MetricItem
        ) {
            val count = item.countc.get()
            val time = item.timec.get()

            val metricCountName = makeMetricName(EMPTY, name, COUNT_SUFFIX)
            val metricTimeName = makeMetricName(EMPTY, name, TIME_SUFFIX)

            // 1.填充count
            metrics[metricCountName] = count

            // 2.填充time
            if (count > 0) {
                metrics[metricTimeName] = time / count
            } else {
                metrics[metricTimeName] = 0
            }

        }

        /**
         * 添加一些JVM的监控指标
         *
         * @param metrics 待填充相关指标的Metrics Map
         */
        private fun metricsJVM(metrics: MutableMap<String, Any>) {
            // JVM的GC信息
            for (mxBean in ManagementFactory.getGarbageCollectorMXBeans()) {
                val name = mxBean.name
                val collectionCount = mxBean.collectionCount
                val collectionTime = mxBean.collectionTime
                makeItemQuantile(name, collectionCount, collectionTime, metrics)
            }

            // JVM线程信息
            val threadMXBean = ManagementFactory.getThreadMXBean()
            if (threadMXBean != null) {
                metrics[makeMetricName(JVM_PREFIX, "Thread", COUNT_SUFFIX)] = threadMXBean.threadCount
            }

            // JVM的JIT编译信息
            val compilationMXBean = ManagementFactory.getCompilationMXBean()
            if (compilationMXBean != null) {
                makeItemQuantile(
                    makeMetricName(JVM_PREFIX, "JIT_Compilation", EMPTY),
                    1,
                    compilationMXBean.totalCompilationTime,
                    metrics
                )
            }

            // JVM的堆内存信息
            val memoryMXBean = ManagementFactory.getMemoryMXBean()
            if (memoryMXBean != null) {
                metrics[makeMetricName(JVM_PREFIX, "Heap_Memory_Usage_MBytes", COUNT_SUFFIX)] =
                    memoryMXBean.heapMemoryUsage.used
                metrics[makeMetricName(JVM_PREFIX, "Heap_Memory_Init_MBytes", COUNT_SUFFIX)] =
                    memoryMXBean.heapMemoryUsage.init
                metrics[makeMetricName(JVM_PREFIX, "Heap_Memory_Max_MBytes", COUNT_SUFFIX)] =
                    memoryMXBean.heapMemoryUsage.max
                metrics[makeMetricName(JVM_PREFIX, "Heap_Memory_Commit_MBytes", COUNT_SUFFIX)] =
                    memoryMXBean.heapMemoryUsage.committed
            }

            // JVM的内存池的内存使用信息(Code_Cache/Metaspace/Compressed_Class_Space/PS_Eden_Space/PS_Survivor_Space/PS_Old_Gen)
            for (memoryPoolMXBean in ManagementFactory.getMemoryPoolMXBeans()) {
                val usage = memoryPoolMXBean.usage
                val name = memoryPoolMXBean.name.replace("\\s|\\'|-", "_")
                metrics[makeMetricName(JVM_PREFIX, name + "_Memory_Usage_MBytes", COUNT_SUFFIX)] = usage.used
                metrics[makeMetricName(JVM_PREFIX, name + "_Memory_Init_MBytes", COUNT_SUFFIX)] = usage.init
                metrics[makeMetricName(JVM_PREFIX, name + "_Memory_Max_MBytes", COUNT_SUFFIX)] = usage.max
                metrics[makeMetricName(JVM_PREFIX, name + "_Memory_Commit_MBytes", COUNT_SUFFIX)] = usage.committed
            }

        }

        /**
         * 添加一个Tomcat的Metrics指标
         *
         * @param metrics 待填充相关指标的Metrics Map
         */
        private fun metricsTomcat(metrics: MutableMap<String, Any>) {

        }

        private fun makeItemQuantile(name: String, count: Long, time: Long, metrics: MutableMap<String, Any>) {
            var metricItem = Metrics.jvmItems[name]
            if (metricItem == null) {
                metricItem = MetricItem()
                metricItem.addSample(name, count, time)
                Metrics.jvmItems[name] = metricItem
            }

            // 计算count
            metrics[makeMetricName(EMPTY, name, COUNT_SUFFIX)] = count - metricItem.countc.get()

            // 计算time
            if (count - metricItem.countc.get() > 0) {
                metrics[makeMetricName(EMPTY, name, TIME_SUFFIX)] =
                    (time - metricItem.timec.get()) / (count - metricItem.countc.get())
            }

            // 重新去构建一个MetricItem, 并将本次数据添加到MetricItem当中...
            metricItem = MetricItem()
            metricItem.addSample(name, count, time)
            Metrics.jvmItems[name] = metricItem
        }
    }
}