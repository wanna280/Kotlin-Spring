package com.wanna.cloud.openfeign

import feign.Feign
import feign.Target

object FeignCircuitBreaker {
    @JvmStatic
    fun builder(): Builder {
        return Builder()
    }

    /**
     * 扩展FeignBuilder, 提供自定义逻辑的Builder, 为Fallback和FallbackFactory提供支持
     */
    class Builder : Feign.Builder() {
        override fun <T : Any?> target(target: Target<T>?): T {
            return build(null).newInstance(target)
        }

        fun <T> target(target: Target<T>, fallback: T): T {
            return target(target, FallbackFactory.Default(fallback))
        }

        fun <T> target(target: Target<T>, fallbackFactory: FallbackFactory<T>): T {
            return build(fallbackFactory).newInstance(target)
        }

        /**
         * 根据FallbackFactory, 去替换掉原来的默认的InvocationHandler
         *
         * @param fallbackFactory fallbackFactory
         */
        private fun build(fallbackFactory: FallbackFactory<*>?): Feign {
            // 设置InvocationHandler为自定义的InvocationHandler
            super.invocationHandlerFactory { target, dispatch ->
                return@invocationHandlerFactory FeignCircuitBreakerInvocationHandler(target, fallbackFactory, dispatch)
            }
            return super.build()
        }
    }
}