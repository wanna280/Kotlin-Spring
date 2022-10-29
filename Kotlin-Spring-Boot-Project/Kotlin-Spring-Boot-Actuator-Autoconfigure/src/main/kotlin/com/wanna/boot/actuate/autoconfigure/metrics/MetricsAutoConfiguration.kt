package com.wanna.boot.actuate.autoconfigure.metrics

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.Order
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.config.MeterFilter

/**
 * 提供Metrics指标监控的自动配置类，为所有的监控指标的处理提供SpringBeanFactory层面的支持
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 */
@EnableConfigurationProperties([MetricsProperties::class])
@Configuration(proxyBeanMethods = false)
open class MetricsAutoConfiguration {

    /**
     * 提供一个统计耗时的时钟对象，暴露到SpringBeanFactory当中，供别的组件使用
     *
     * @return Clock
     */
    @Bean
    @ConditionalOnMissingBean
    open fun clock(): Clock = Clock.SYSTEM

    /**
     * 对于MeterRegistry的后置处理的BeanPostProcessor，用于将[MeterBinder]、[MeterFilter]以及[MeterRegistryCustomizer]
     * 都去merge到MeterRegistry当中，对于MeterRegistry的探测，就是从SpringBeanFactory当中通过BeanPostProcessor去进行探测的
     *
     * @param meterBinders 需要去应用的MeterBinder(真正的指标数据)，从SpringBeanFactory自动装配
     * @param meterFilters MeterFilters，从SpringBeanFactory自动装配
     * @param customizers MeterRegistry的自定义化器列表，从SpringBeanFactory自动装配
     * @param metricsProperties 监控指标的配置信息
     * @param applicationContext ApplicationContext
     * @return MeterRegistry的后置处理器BeanPostProcessor
     */
    @Bean
    open fun meterRegistryPostProcessor(
        meterBinders: List<MeterBinder>,
        meterFilters: List<MeterFilter>,
        customizers: List<MeterRegistryCustomizer<*>>,
        metricsProperties: MetricsProperties,
        applicationContext: ApplicationContext,
    ): MeterRegistryPostProcessor {
        return MeterRegistryPostProcessor(
            meterBinders, meterFilters, customizers,
            metricsProperties, applicationContext
        )
    }

    /**
     * 提供基于属性([MetricsProperties])去进行实现的[MeterFilter]
     *
     * @param metricsProperties MetricsProperties
     * @return PropertiesMeterFilter
     */
    @Bean
    @Order(0)
    open fun propertiesMeterFilter(metricsProperties: MetricsProperties): PropertiesMeterFilter {
        return PropertiesMeterFilter(metricsProperties)
    }
}