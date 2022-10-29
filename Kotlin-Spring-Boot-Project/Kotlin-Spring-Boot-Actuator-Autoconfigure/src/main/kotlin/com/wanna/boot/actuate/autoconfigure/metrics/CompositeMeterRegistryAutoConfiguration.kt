package com.wanna.boot.actuate.autoconfigure.metrics

import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import
import io.micrometer.core.instrument.MeterRegistry

/**
 * 提供对于组合的[MeterRegistry]的功能的SpringBoot自动配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 *
 * @see CompositeMeterRegistryConfiguration
 */
@ConditionalOnClass(name = ["io.micrometer.core.instrument.composite.CompositeMeterRegistry"])
@Import([CompositeMeterRegistryConfiguration::class])
@Configuration(proxyBeanMethods = false)
open class CompositeMeterRegistryAutoConfiguration