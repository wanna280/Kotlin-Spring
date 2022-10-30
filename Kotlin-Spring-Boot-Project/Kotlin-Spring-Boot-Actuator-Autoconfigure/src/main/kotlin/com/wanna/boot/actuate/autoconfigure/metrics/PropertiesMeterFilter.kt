package com.wanna.boot.actuate.autoconfigure.metrics

import io.micrometer.core.instrument.config.MeterFilter

/**
 * 基于配置文件([MetricsProperties])的方式去实现[MeterFilter]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 *
 * @param metricsProperties 监控指标的配置文件
 */
open class PropertiesMeterFilter(private val metricsProperties: MetricsProperties) : MeterFilter {

}