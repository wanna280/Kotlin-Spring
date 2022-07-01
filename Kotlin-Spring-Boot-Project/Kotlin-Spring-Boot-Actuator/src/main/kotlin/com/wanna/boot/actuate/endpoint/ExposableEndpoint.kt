package com.wanna.boot.actuate.endpoint

/**
 * 可以支持进行暴露的Endpoint，它需要提供该Endpoint下的所有的Operation的列表
 *
 * @see Operation
 * @param O 要去进行暴露的Operation的具体类型
 */
interface ExposableEndpoint<O : Operation> {
    /**
     * 获取要去进行暴露的Endpoint的Id
     *
     * @return EndpointId
     */
    fun getEndpointId(): EndpointId

    /**
     * 获取该Endpoint下的所有的Operation列表
     *
     * @return 该Endpoint下要去进行暴露的Operation列表
     */
    fun getOperations(): Collection<O>
}