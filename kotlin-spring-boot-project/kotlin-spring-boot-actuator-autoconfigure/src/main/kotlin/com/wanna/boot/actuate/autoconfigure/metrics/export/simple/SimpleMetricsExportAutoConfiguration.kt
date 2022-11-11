package com.wanna.boot.actuate.autoconfigure.metrics.export.simple

import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.simple.SimpleConfig
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

/**
 * 提供对于SimpleMeterRegistry的自动配置
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 */
@EnableConfigurationProperties([SimpleProperties::class])
@ConditionalOnBean([Clock::class])
@Configuration(proxyBeanMethods = false)
open class SimpleMetricsExportAutoConfiguration {
    @Bean
    open fun simpleMetricRegistry(simpleConfig: SimpleConfig, clock: Clock): SimpleMeterRegistry {
        return SimpleMeterRegistry(simpleConfig, clock)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun simpleConfig(simpleProperties: SimpleProperties): SimpleConfig {
        return SimplePropertiesConfigAdapter(simpleProperties)
    }
}