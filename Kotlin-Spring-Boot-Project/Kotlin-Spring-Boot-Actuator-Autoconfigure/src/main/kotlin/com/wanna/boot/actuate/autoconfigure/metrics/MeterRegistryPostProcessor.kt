package com.wanna.boot.actuate.autoconfigure.metrics

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.core.instrument.config.MeterFilter

/**
 * 提供对于[MeterRegistry]的自定义工作，通过委托[MeterRegistryConfigurer]去完成真正的定义
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 * @see MeterRegistry
 * @see MeterRegistryConfigurer
 *
 * @param meterBinders 各个指标对象
 * @param meterFilters 对于指标的过滤器
 * @param customizers 对于MeterRegistry的自定义化器
 * @param metricsProperties Metrics相关的配置信息
 * @param applicationContext ApplicationContext
 */
open class MeterRegistryPostProcessor(
    private val meterBinders: List<MeterBinder>,
    private val meterFilters: List<MeterFilter>,
    private val customizers: List<MeterRegistryCustomizer<*>>,
    private val metricsProperties: MetricsProperties,
    private val applicationContext: ApplicationContext
) : BeanPostProcessor {

    /**
     * MeterRegistryConfigurer，对于真正的[MeterRegistry]的自定义
     *
     * @see MeterRegistry
     */
    @Volatile
    private var configurer: MeterRegistryConfigurer? = null

    /**
     * 对Bean去进行后置处理时，如果遇到了一个[MeterRegistry]的话，那么它就需要交给[MeterRegistryConfigurer]去进行自定义操作
     *
     * @param beanName beanName
     * @param bean bean
     * @return bean
     */
    override fun postProcessAfterInitialization(beanName: String, bean: Any): Any? {
        // 如果该Bean是一个MeterRegistry的话，那么我们就需要交给MeterRegistryConfigurer去进行自定义...
        if (bean is MeterRegistry) {
            getConfigurer().configure(bean)
        }
        return bean
    }

    /**
     * 获取到[MeterRegistryConfigurer]，提供对于[MeterRegistry]去执行真正的自定义
     *
     * @return MeterRegistryConfigurer
     */
    private fun getConfigurer(): MeterRegistryConfigurer {
        if (this.configurer == null) {
            // 检查一下是否存在有CompositeMeterRegistry？
            val hasCompositeMeterRegistry =
                this.applicationContext.getBeanNamesForType(CompositeMeterRegistry::class.java, true, false).isEmpty()

            // 构建出来一个MeterRegistryConfigurer，将MeterBinder/MeterFilter/MeterRegistryCustomizer
            // 去merge到给定的MeterRegistry当中来
            this.configurer =
                MeterRegistryConfigurer(
                    meterBinders, meterFilters, customizers,
                    hasCompositeMeterRegistry, metricsProperties.useGlobalRegistry
                )
        }
        return this.configurer!!
    }
}