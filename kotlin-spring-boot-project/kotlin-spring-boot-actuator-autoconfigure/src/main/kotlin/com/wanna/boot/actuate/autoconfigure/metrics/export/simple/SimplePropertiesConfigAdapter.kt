package com.wanna.boot.actuate.autoconfigure.metrics.export.simple

import io.micrometer.core.instrument.simple.SimpleConfig

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 */
class SimplePropertiesConfigAdapter(private val simpleProperties: SimpleProperties) : SimpleConfig {
    override fun get(key: String): String? = null

    override fun prefix(): String = "management.simple.metrics.export"
}