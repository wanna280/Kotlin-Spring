package com.wanna.boot.actuate.autoconfigure.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.core.instrument.config.MeterFilter
import com.wanna.common.logging.LoggerFactory

/**
 * 根据提供的[MeterBinder]、[MeterFilter]以及[MeterRegistryCustomizer], 提供对于给定的[MeterRegistry]的自定义
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 *
 * @param meterBinders MeterBinders(应用到组合的MeterRegistry当中)
 * @param meterFilters MeterFilters(应用到非组合的MeterRegistry当中)
 * @param customizers MeterRegistryCustomizers
 * @param addToGlobalRegistry 是否需要将给定的MeterRegistry去添加到全局的MeterRegistry当中
 * @param hasCompositeMeterRegistry BeanFactory当中是否存在有组合所有的MeterRegistry的CompositeMeterRegistry? 
 *
 * @see MeterFilter
 * @see MeterBinder
 * @see MeterRegistryCustomizer
 * @see AutoConfiguredCompositeMeterRegistry
 * @see CompositeMeterRegistry
 */
open class MeterRegistryConfigurer(
    private val meterBinders: List<MeterBinder>,
    private val meterFilters: List<MeterFilter>,
    private val customizers: List<MeterRegistryCustomizer<*>>,
    private val addToGlobalRegistry: Boolean,
    private val hasCompositeMeterRegistry: Boolean
) {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(MeterRegistryConfigurer::class.java)
    }

    /**
     * 根据[MeterBinder]、[MeterFilter]以及[MeterRegistryCustomizer]去对[MeterRegistry]进行自定义操作
     *
     * @param registry 需要去进行自定义的MeterRegistry
     */
    open fun configure(registry: MeterRegistry) {

        // 对于customize方法必须在最前面, 因为它们可能会自定义的Tags和修改Timer或者是一些总结性的配置信息
        customize(registry)

        // 只有在不是AutoConfiguredCompositeMeterRegistry情况, 才去进行添加Filters
        // 我们需要将Filter去应用给非组合的MeterRegistry当中, 而不是组合的MeterRegistry里
        if (registry !is AutoConfiguredCompositeMeterRegistry) {
            addFilters(registry)
        }

        // 如果没有CompositeMeterRegistry这个用于去进行聚合的MeterRegistry的话, 那么我们必须将它去应用给当前MeterRegistry
        // 如果存在有CompositeMeterRegistry的话, 那么我们只有在当前就是CompositeMeterRegistry的情况才去添加, 不对单个的MeterRegistry去进行添加了
        if (!this.hasCompositeMeterRegistry || registry is CompositeMeterRegistry) {
            addBinders(registry)
        }

        // 如果需要将当前MeterRegistry去注册到全局的MeterRegistry当中的话
        if (addToGlobalRegistry && registry != Metrics.globalRegistry) {
            Metrics.addRegistry(registry)
        }
    }

    /**
     * 对于给定的[MeterRegistry], 使用[MeterRegistryCustomizer]去进行自定义
     *
     * @param registry 需要去进行自定义的MeterRegistry
     */
    @Suppress("UNCHECKED_CAST")
    private fun customize(registry: MeterRegistry) {
        this.customizers.forEach {
            try {
                (it as MeterRegistryCustomizer<MeterRegistry>).customize(registry)
            } catch (ex: Exception) {
                logger.error("使用[$it]去对[$registry]去进行自定义失败", ex)
            }
        }
    }

    /**
     * 添加所有的[MeterFilter]到给定的[MeterRegistry]当中
     *
     * @param registry MeterRegistry
     */
    private fun addFilters(registry: MeterRegistry) {
        meterFilters.forEach { registry.config().meterFilter(it) }
    }

    /**
     * 添加所有的[MeterBinder]到给定的[MeterRegistry]当中
     *
     * @param registry MeterRegistry
     */
    private fun addBinders(registry: MeterRegistry) {
        meterBinders.forEach { it.bindTo(registry) }
    }
}