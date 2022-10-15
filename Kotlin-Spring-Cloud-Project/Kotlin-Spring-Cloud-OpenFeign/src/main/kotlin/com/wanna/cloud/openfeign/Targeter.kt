package com.wanna.cloud.openfeign

import feign.Feign

interface Targeter {
    fun <T> target(
        factory: FeignClientFactoryBean,
        feign: Feign.Builder,
        context: FeignContext,
        target: feign.Target.HardCodedTarget<T>
    ): T
}