package com.wanna.boot.actuate.endpoint

/**
 * 抽象的Endpoint的实现
 */
abstract class AbstractExposableEndpoint<O : Operation>(
    private val id: EndpointId,
    private val operations: Collection<O>
) : ExposableEndpoint<O> {
    override fun getEndpointId() = id
    override fun getOperations() = operations
}