package com.wanna.boot.actuate.autoconfigure.metrics

import com.wanna.boot.context.properties.ConfigurationProperties
import  io.micrometer.core.instrument.MeterRegistry

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 */
@ConfigurationProperties("management.metrics")
open class MetricsProperties {

    /**
     * 是否需要将[MeterRegistry]去应用到全局的[MeterRegistry]当中? 默认为true
     */
    var useGlobalRegistry: Boolean = true
}