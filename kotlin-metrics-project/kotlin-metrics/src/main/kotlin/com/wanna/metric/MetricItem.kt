package com.wanna.metric

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.Nullable

/**
 * 记录的是一个监控指标的相关数据, 比如消耗的时间/数量等数据
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 *
 * @param saveSample 是否需要去保存采样指标?
 */
class MetricItem(private val saveSample: Boolean = false) {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(MetricItem::class.java)
    }

    /**
     * 指标名
     */
    internal var name = ""

    /**
     * 统计耗时的计数器
     */
    internal var timec = AtomicLong()

    /**
     * 统计数量的计数值
     */
    internal var countc = AtomicLong()

    /**
     * 添加一条统计记录的数据到当前[MetricItem]当中
     *
     * @param name 指标名
     * @param count 统计值
     * @param time 消耗时间
     */
    fun addSample(name: String, count: Long, time: Long) {
        this.name = name
        this.timec.addAndGet(time)
        this.countc.addAndGet(count)
    }

    /**
     * 添加一条统计记录的数据到当前[MetricItem]当中
     *
     * @param name 指标名
     * @param count 统计值
     * @param time 消耗时间
     * @param tags 追加的Tags
     */
    fun addSample(name: String, count: Long, time: Long, @Nullable tags: Map<String, String>?) {
        // 先将name/count/time去进行记录下来
        addSample(name, count, time)

        // 接着需要去计算采样指标...
    }


    /**
     * copy得到一个新的[MetricItem], 并将当前[MetricItem]当中的相关指标数据去进行清空
     *
     * @return copy得到的一个新的[MetricItem]
     */
    @Synchronized
    fun dumpAndClearItem(): MetricItem {
        val metricItem = MetricItem()
        metricItem.countc = this.countc
        metricItem.timec = this.timec

        this.countc = AtomicLong()
        this.timec = AtomicLong()
        return metricItem
    }

    /**
     * 计算采样
     */
    private fun sampleCalc(
        @Nullable tags: Map<String, String>?,
        count: Long,
        time: Long,
        useTag: Boolean,
        tagHasSample: Boolean,
        primaryKey: Map<Any, String>
    ) {

    }
}