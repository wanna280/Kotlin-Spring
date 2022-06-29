package com.wanna.boot.actuate.endpoint

/**
 * Endpoint的Supplier，负责提供Endpoint的获取工作
 */
interface EndpointsSupplier<E : ExposableEndpoint<*>> {
    fun getEndpoints(): Collection<E>
}