package com.wanna.cloud.openfeign

import com.wanna.common.logging.LoggerFactory

/**
 * Fallback Factory
 */
interface FallbackFactory<T> {
    fun create(cause: Throwable): T

    /**
     * Fallback Factory的默认实现
     *
     * @param constant Fallback
     */
    class Default<T>(private val constant: T) : FallbackFactory<T> {
        private val logger = LoggerFactory.getLogger(Default::class.java)
        override fun create(cause: Throwable): T {
            if (logger.isTraceEnabled) {
                logger.trace("fallback-->原因是[${cause.message}]", cause)
            }
            return constant
        }
    }
}