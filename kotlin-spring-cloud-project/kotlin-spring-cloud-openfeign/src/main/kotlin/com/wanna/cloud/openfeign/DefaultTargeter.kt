package com.wanna.cloud.openfeign

import feign.Feign
import feign.Target

/**
 * Targeter的默认实现; 
 * 没有任何别的相关的功能, 直接使用FeignBuilder.target去创建代理对象, 在运行时直接调用相关的方法; 
 *
 * @see target
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