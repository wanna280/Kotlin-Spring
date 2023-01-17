package com.wanna.metric

import java.util.concurrent.atomic.AtomicLong

/**
 * 供Metrics监控指标使用的[AtomicLong]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 */
open class MetricAtomicLong {

    private val value = AtomicLong()

    open fun set(newValue: Long) {
        value.set(newValue)
    }

    open fun addAndGet(newValue: Long): Long = value.addAndGet(newValue)

    open fun get(): Long = value.get()
}