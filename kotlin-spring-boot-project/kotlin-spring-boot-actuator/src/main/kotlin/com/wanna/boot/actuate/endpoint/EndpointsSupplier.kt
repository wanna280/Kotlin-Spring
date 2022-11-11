package com.wanna.boot.actuate.endpoint

/**
 * Endpoint的Supplier，负责提供Endpoint的获取工作
 *
 * @param E 要获取的Endpoint的类型
 */
interface EndpointsSupplier<E : ExposableEndpoint<*>> {

    /**
     * 获取到当前Supplier当中的Endpoint的列表
     *
     * @return Endpoint列表
     */
    fun getEndpoints(): Collection<E>
}