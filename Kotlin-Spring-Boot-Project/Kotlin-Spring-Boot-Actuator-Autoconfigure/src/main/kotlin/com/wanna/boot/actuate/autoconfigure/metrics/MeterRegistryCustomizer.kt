package com.wanna.boot.actuate.autoconfigure.metrics

import io.micrometer.core.instrument.MeterRegistry

/**
 * 提供对于[MeterRegistry]的自定义操作的自定义化器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 * @param T  MeterRegistry类型
 */
interface MeterRegistryCustomizer<T : MeterRegistry> {

    /**
     * 对MeterRegistry去进行自定义操作
     *
     * @param registry 需要去进行自定义的MeterRegistry
     */
    fun customize(registry: T)
}