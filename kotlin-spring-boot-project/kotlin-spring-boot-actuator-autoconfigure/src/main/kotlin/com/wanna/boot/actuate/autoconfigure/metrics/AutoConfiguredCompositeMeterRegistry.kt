package com.wanna.boot.actuate.autoconfigure.metrics

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.composite.CompositeMeterRegistry

/**
 * 提供SpringBoot当中需要使用到的对于[CompositeMeterRegistry]的实现, 提供对于组合的[MeterRegistry]的实现.
 * 因为`io.micrometer.core.instrument`包当中并未提供相关的实现, 因此我们去进行自定义
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 *
 * @see CompositeMeterRegistry
 * @see MeterRegistry
 *
 * @param clock 时钟Clock
 * @param registries 需要去组合的多个MeterRegistry
 */
open class AutoConfiguredCompositeMeterRegistry(clock: Clock, registries: List<MeterRegistry>) :
    CompositeMeterRegistry(clock, registries)