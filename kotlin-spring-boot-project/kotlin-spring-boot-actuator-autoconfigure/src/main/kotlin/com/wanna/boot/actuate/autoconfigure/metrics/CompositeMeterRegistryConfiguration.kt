package com.wanna.boot.actuate.autoconfigure.metrics

import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Primary
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.composite.CompositeMeterRegistry

/**
 * 用于给SpringBeanFactory当中去导入一个[CompositeMeterRegistry]的配置类, 它会自动收集起来所有的[MeterRegistry],
 * 并merge到一个[CompositeMeterRegistry]当中来, 最终我们要去进行使用的就是[AutoConfiguredCompositeMeterRegistry]
 * 这个[CompositeMeterRegistry], 去提供最终的Endpoint的对外暴露; 
 *
 * Eg:[io.micrometer.core.instrument.simple.SimpleMeterRegistry]
 *
 * Note: 只有在BeanFactory当中已经存在有[MeterRegistry]的情况下我们才需要去进行装配
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 */
@ConditionalOnClass(value = [io.micrometer.core.instrument.MeterRegistry::class])
@ConditionalOnBean([MeterRegistry::class])
@Configuration(proxyBeanMethods = false)
open class CompositeMeterRegistryConfiguration {

    /**
     * 给SpringBeanFactory当中去导入一个组合了所有的[MeterRegistry]的[CompositeMeterRegistry]实现; 
     * 将所有的[MeterRegistry]去组合在一起, 合并提供最终的Metrics监控功能
     *
     * @param clock Clock in Spring BeanFactory
     * @param registries MeterRegistries in Spring BeanFactory
     * @return 需要给SpringBeanFactory当中去导入的CompositeMeterRegistry
     */
    @Bean
    @Primary
    open fun compositeMeterRegistry(clock: Clock, registries: List<MeterRegistry>): CompositeMeterRegistry {
        return AutoConfiguredCompositeMeterRegistry(clock, registries)
    }
}