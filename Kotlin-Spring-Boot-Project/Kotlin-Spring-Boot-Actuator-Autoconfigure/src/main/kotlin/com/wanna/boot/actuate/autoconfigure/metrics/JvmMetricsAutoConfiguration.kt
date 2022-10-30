package com.wanna.boot.actuate.autoconfigure.metrics

import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.*

/**
 * 提供一些对于JVM的指标监控的自动配置类，对于这些Metrics对象，最终会被SpringBeanFactory
 * 去收集到[io.micrometer.core.instrument.MeterRegistry]当中
 *
 * Note: 只有在SpringBeanFactory当中存在有[io.micrometer.core.instrument.MeterRegistry]的情况下才需要去进行装配
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 * @see io.micrometer.core.instrument.MeterRegistry
 * @see MeterRegistryPostProcessor
 * @see MeterRegistryConfigurer
 */
@ConditionalOnClass(name = ["io.micrometer.core.instrument.MeterRegistry"])
@ConditionalOnBean([MeterRegistry::class])
@Configuration(proxyBeanMethods = false)
open class JvmMetricsAutoConfiguration {

    /**
     * 提供JVM的Gc监控指标的Bean
     *
     * @return JvmGcMetrics
     */
    @Bean
    @ConditionalOnMissingBean
    open fun jvmGcMetrics(): JvmGcMetrics = JvmGcMetrics()

    /**
     * 提供JVM的堆指标的压力指标的Bean
     *
     * @return JvmHeapPressureMetrics
     */
    @Bean
    @ConditionalOnMissingBean
    open fun jvmHeapPressureMetrics(): JvmHeapPressureMetrics = JvmHeapPressureMetrics()

    /**
     * 提供JVM的内存指标的监控的Bean
     *
     * @return JvmMemoryMetrics
     */
    @Bean
    @ConditionalOnMissingBean
    open fun jvmMemoryMetrics(): JvmMemoryMetrics = JvmMemoryMetrics()

    /**
     * 提供JVM的线程指标的Bean
     *
     * @return JvmThreadMetrics
     */
    @Bean
    @ConditionalOnMissingBean
    open fun jvmThreadMetrics(): JvmThreadMetrics = JvmThreadMetrics()

    /**
     * 提供JVM的ClassLoader的指标的Bean
     *
     * @return ClassLoaderMetrics
     */
    @Bean
    @ConditionalOnMissingBean
    open fun classLoaderMetrics(): ClassLoaderMetrics = ClassLoaderMetrics()

    /**
     * 提供JVM的信息的监控指标的Bean
     *
     * @return JvmInfoMetrics
     */
    @Bean
    @ConditionalOnMissingBean
    open fun jvmInfoMetrics(): JvmInfoMetrics = JvmInfoMetrics()

    /**
     * 提供JVM的编译器相关的指标的Bean
     *
     * @return JvmCompilationMetrics
     */
    @Bean
    @ConditionalOnMissingBean
    open fun jvmCompilationMetrics(): JvmCompilationMetrics = JvmCompilationMetrics()
}