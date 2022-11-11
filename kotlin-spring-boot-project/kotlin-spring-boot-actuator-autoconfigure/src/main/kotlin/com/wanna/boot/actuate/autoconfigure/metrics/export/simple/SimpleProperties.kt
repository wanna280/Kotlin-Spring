package com.wanna.boot.actuate.autoconfigure.metrics.export.simple

import com.wanna.boot.context.properties.ConfigurationProperties

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 */
@ConfigurationProperties("management.simple.metrics.export")
open class SimpleProperties {

    /**
     * 是否开启？
     */
    var enabled: Boolean = true
}