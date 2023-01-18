package com.wanna.metric

import com.wanna.metric.server.MetricServerHelper
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.Nullable

/**
 * 记录监控的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 */
object Metrics {

    /**
     * Logger
     */
    @JvmStatic
    private val logger = LoggerFactory.getLogger(Metrics::class.java)

    /**
     * 记录Metrics去处理recordMany的耗时情况
     */
    private const val METRIC_NAME = "Metrics_Handle_RecordMany"

    /**
     * 监控指标Item, 记录的是指标对应的平均值, Key-指标名, Value-该指标对应的监控指标数据
     */
    internal val avgItems = ConcurrentHashMap<String, MetricItem>()

    /**
     * Value类型的指标Item, 针对某个指标需要记录一个数量的情况去进行使用, Key为指标名, Value是该指标对应的统计值数量
     *
     * @see Metrics.recordSize
     * @see Metrics.recordValue
     */
    internal val values = ConcurrentHashMap<String, MetricAtomicLong>()

    /**
     * JVM的监控指标, Key-指标名, Value-该指标名对应的监控指标数据
     */
    internal val jvmItems = ConcurrentHashMap<String, MetricItem>()

    /**
     * 当前的统计结果, 不允许有写的情况, 采用的是直接替换的方式, 线程安全
     */
    internal var currentItems: Map<String, Any> = emptyMap()

    /**
     * 当前的Setting的统计结果, 不允许有写的情况, 采用的是直接替换的方式, 线程安全
     */
    internal var currentSettingItems: Map<String, Any> = emptyMap()

    init {
        // 添加Metrics的定时任务, 实现定时将指标汇总到currentItems/currentSettingItems当中...
        MetricScheduleTask.loadSchedule()

        // 启动MetricServer, 负责使用HTTPServer的方式去进行Metrics指标的暴露...
        MetricServerHelper.load()
    }


    /**
     * 记录一条统计数据, 统计值+1, 统计的时间窗口为1min, 最终指标反映的是时间的平均值
     *
     * @param name 指标名
     */
    @JvmStatic
    fun recordOne(name: String) = recordMany(name, 1L, 0L, false, null)

    /**
     * 记录一条统计数据, 统计值+1, 时间累加; 统计的时间窗口为1min, 最终指标反映的是时间的平均值
     *
     * @param name 指标名
     * @param time 消耗时间
     */
    @JvmStatic
    fun recordOne(name: String, time: Long) = recordMany(name, 1L, time, false, null)

    /**
     * 记录一条统计数据, 统计值+1, 统计的时间窗口为1min, 最终指标反映的是时间的平均值
     *
     * @param name 指标名
     * @param tags 追加的Tags
     */
    @JvmStatic
    fun recordOne(name: String, @Nullable tags: Map<String, String>?) = recordMany(name, 1L, 0L, false, tags)

    /**
     * 在recordOne的基础上, 新增对于样本的采集, 用于统计监控指标的分位数, 比如P50/P90/P95/P99
     *
     * @param name 指标名
     * @param time 消耗时间
     */
    @JvmStatic
    fun recordQuantile(name: String, time: Long) = recordMany(name, 1L, time, true, null)

    /**
     * 在recordOne的基础上, 新增对于样本的采集, 用于统计监控指标的分位数, 比如P50/P90/P95/P99, 可追加Tags
     *
     * @param name 指标名
     * @param time 消耗时间
     * @param tags 追加Tags
     */
    @JvmStatic
    fun recordQuantile(name: String, time: Long, @Nullable tags: Map<String, String>?) =
        recordMany(name, 1L, time, true, tags)


    /**
     * 记录一条统计数据, 统计值+count, 统计的时间窗口为1min, 最终指标反映的是时间的平均值
     *
     * @param name 指标名
     * @param count 统计值需要增加的数量
     */
    @JvmStatic
    fun recordMany(name: String, count: Long) = recordMany(name, count, 0L, false, null)

    /**
     * 记录一条统计数据, 统计值+count, 统计的时间窗口为1min, 最终指标反映的是时间的平均值
     *
     * @param name 指标名
     * @param count 统计值需要增加的数量
     * @param time 消耗时间
     * @param saveSample 是否需要去进行样本的采样?
     * @param tags 追加Tags
     */
    @JvmStatic
    fun recordMany(name: String, count: Long, time: Long, saveSample: Boolean, @Nullable tags: Map<String, String>?) {
        try {
            val start = System.nanoTime()

            // 获取到当前指标对应的MetricItem
            val monitorItem = getOrNewMetricItem(name, saveSample)
            // 将当前统计数据添加到MetricItem当中去
            monitorItem.addSample(name, count, time, tags)

            // 记录一下本次recordMany打指标所花费的时间
            val metricsItem = getOrNewMetricItem(METRIC_NAME, false)
            metricsItem.addSample(METRIC_NAME, 1L, (System.nanoTime() - start) / 1000)
        } catch (ex: Throwable) {
            logger.error("Metrics handle error, name={}", name, ex)
        }
    }

    /**
     * 检查在[avgItems]当中之前是否已经存在有给定的指标名对应的[MetricItem]?
     * 如果之前已经存在, 返回之前的[MetricItem]; 如果之前不存在, 返回新创建的[MetricItem]
     *
     * @param name 指标名
     * @param saveSample 是否需要保存采样数据?
     * @return 针对给定的指标名获取到的对应的MetricItem
     */
    @JvmStatic
    private fun getOrNewMetricItem(name: String, saveSample: Boolean): MetricItem {
        // 如果之前已经存在, 那么返回之前已经存在的; 如果之前不存在, 返回新的MetricItem
        return avgItems[name] ?: avgItems.computeIfAbsent(name) { MetricItem(saveSample) }
    }

    /**
     * 对于单个指标去记录一个数量监控(对于下次recordSize时, 统计方式为将会覆盖掉之前的数量指标)
     *
     * @param name 指标名
     * @param size 针对该指标要去记录的统计数量
     */
    @JvmStatic
    fun recordSize(name: String, size: Long) {
        var metricAtomicLong = values[name]
        if (metricAtomicLong == null) {
            metricAtomicLong = values.computeIfAbsent(name) { MetricAtomicLong() }
        }
        metricAtomicLong.set(size)
    }

    /**
     * 对于单个指标去记录一个数量监控(对于下次recordValue时, 统计方式为会将该指标去进行累加)
     *
     * @param name 指标名
     * @param value 针对该指标要去记录的数量指标
     */
    @JvmStatic
    fun recordValue(name: String, value: Long) {
        var metricAtomicLong = values[name]
        if (metricAtomicLong == null) {
            metricAtomicLong = values.computeIfAbsent(name) { MetricAtomicLong() }
        }
        metricAtomicLong.addAndGet(value)
    }

    /**
     * 获取当前的监控指标快照
     *
     * @return 当前监控指标快照
     */
    @JvmStatic
    fun getCurrentItems(): Map<String, Any> = this.currentItems

    /**
     * 获取当前的Setting监控指标快照
     *
     * @return 当前Setting监控指标快照
     */
    @JvmStatic
    fun getCurrentSettingItems(): Map<String, Any> = this.currentSettingItems
}