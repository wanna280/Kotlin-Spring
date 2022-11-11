package com.wanna.boot.actuate.autoconfigure.metrics

import com.wanna.boot.actuate.metrics.MetricsEndpoint
import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import io.micrometer.core.instrument.MeterRegistry

/**
 * 提供对于MetricsEndpoint的自动装配，利用[MeterRegistry]去实现[MetricsEndpoint]的暴露
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 */
@ConditionalOnClass(name = ["io.micrometer.core.annotation.Timed"])
@Configuration(proxyBeanMethods = false)
open class MetricsEndpointAutoConfiguration {

    /**
     * 给SpringBeanFactory当中去导入一个MetricsEndpoint，提供对于监控指标的Endpoint的暴露
     *
     * @param registry MeterRegistry(指标注册中心，所有的监控指标都注册到这里了)，
     * 最终注入的应该是[AutoConfiguredCompositeMeterRegistry]对象，所有的其他[MeterRegistry]
     * 都会被Spring去收集到这个[AutoConfiguredCompositeMeterRegistry]当中来，我们只需要注入
     * 这个即可获取到所有的普通[MeterRegistry]，例如[io.micrometer.core.instrument.simple.SimpleMeterRegistry]
     *
     * @return 需要去进行自动装配到SpringBeanFactory的MetricsEndpoint
     */
    @Bean
    @ConditionalOnBean(value = [MeterRegistry::class])
    open fun metricsEndpoint(registry: MeterRegistry): MetricsEndpoint {
        return MetricsEndpoint(registry)
    }
}