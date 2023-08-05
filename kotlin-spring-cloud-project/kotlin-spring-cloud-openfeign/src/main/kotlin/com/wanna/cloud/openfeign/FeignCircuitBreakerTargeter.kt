package com.wanna.cloud.openfeign

import feign.Feign
import feign.Target

/**
 * Feign的CircuitBreaker(熔断器, 断路器)的Targeter;
 * 在DefaultTargeter的基础上, 增加了fallback机制, 支持处理@FeignClient上的fallback和fallback机制;
 *
 * @see DefaultTargeter
 * @see FeignClient.fallback
 * @see FeignClient.fallbackFactory
 */
class FeignCircuitBreakerTargeter : Targeter {

    @Suppress("UNCHECKED_CAST")
    override fun <T> target(
        factory: FeignClientFactoryBean,
        feign: Feign.Builder,
        context: FeignContext,
        target: Target.HardCodedTarget<T>
    ): T {
        // 如果不是CircuitBreaker, 那么直接调用调用target
        if (feign !is FeignCircuitBreaker.Builder) {
            return feign.target(target)
        }
        // 如果是CircuitBreaker, 那么需要解析fallback和fallbackFactory
        val fallback = factory.fallback
        val fallbackFactory = factory.fallbackFactory
        val contextId = factory.contextId!!
        if (fallback != null) {
            return feign.target(target, context.getInstance(contextId, fallback) as T)
        }
        if (fallbackFactory != null) {
            return feign.target(target, context.getInstance(contextId, fallbackFactory) as FallbackFactory<T>)
        }
        return feign.target(target)
    }

}