package com.wanna.boot.actuate.autoconfigure.metrics.tasks

import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.lang.Nullable
import com.wanna.framework.scheduling.concurrent.ThreadPoolTaskExecutor
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

/**
 * [ThreadPoolTaskExecutor]的指标暴露的自动配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 *
 * @see MeterRegistry
 */
@ConditionalOnClass(value = [io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics::class])
@ConditionalOnBean(value = [Executor::class, MeterRegistry::class])  // 在SpringBeanFactory当中已经存在有MeterRegistry的情况下才生效
@Configuration(proxyBeanMethods = false)
open class TaskExecutorMetricsAutoConfiguration {

    /**
     * 自动注入Spring BeanFactory当中的所有的TaskExecutor的Metrics指标到MeterRegistry当中
     *
     * @param executors SpringBeanFactory当中维护的所有的Executor列表(key-beanName, Value-Executor)
     * @param registry 维护所有的Metrics的MeterRegistry
     *
     * @see ThreadPoolTaskExecutor
     */
    @Autowired
    open fun bindTaskExecutorsToRegistry(executors: Map<String, Executor>, registry: MeterRegistry) {
        executors.forEach { (name, executor) ->
            if (executor is ThreadPoolTaskExecutor) {
                monitor(registry, safeGetThreadPoolExecutor(executor), name)
            }
        }
    }

    /**
     * 将给定的[ThreadPoolExecutor]去注册相关的监控指标([ExecutorServiceMetrics])到[MeterRegistry]当中来
     *
     * @param registry MeterRegistry(维护各种各样的指标数据)
     * @param executor ThreadPoolExecutor(需要去维护监控指标的线程池)
     * @param name threadPoolName(beanName)
     */
    private fun monitor(registry: MeterRegistry, @Nullable executor: ThreadPoolExecutor?, name: String) {
        executor ?: return

        // 创建一个ExecutorServiceMetrics, 并将它去绑定到MeterRegistry当中...¬
        ExecutorServiceMetrics(executor, name, emptyList()).bindTo(registry)
    }

    /**
     * 安全地从给定的Spring实现的[ThreadPoolTaskExecutor]当中去获取到内部的[ThreadPoolExecutor]
     *
     * @param executor ThreadPoolTaskExecutor
     * @return 如果获取到了ThreadPoolExecutor, 那么return true; 如果无法获取到, 那么return null
     */
    @Nullable
    private fun safeGetThreadPoolExecutor(executor: ThreadPoolTaskExecutor): ThreadPoolExecutor? {
        return try {
            executor.getThreadPoolExecutor()
        } catch (ex: Exception) {
            null
        }
    }

}