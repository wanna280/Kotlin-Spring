package com.wanna.cloud.openfeign

import feign.Feign
import feign.Target

/**
 * Targeter的默认实现
 */
open class DefaultTargeter : Targeter {
    override fun <T> target(
        factory: FeignClientFactoryBean,
        feign: Feign.Builder,
        context: FeignContext,
        target: Target.HardCodedTarget<T>
    ): T {
        return feign.target(target)
    }
}