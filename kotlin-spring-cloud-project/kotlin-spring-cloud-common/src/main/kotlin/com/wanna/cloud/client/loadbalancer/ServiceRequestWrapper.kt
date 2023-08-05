package com.wanna.cloud.client.loadbalancer

import com.wanna.cloud.client.ServiceInstance
import com.wanna.framework.web.http.client.ClientHttpRequest

/**
 * SpringCloud的ServiceInstance的Wrapper, 目的是将request去进行包装, 将原始的uri的host部分替换成为ServiceInstance的host
 *
 * @param request 原始的客户端请求
 * @param loadBalancerClient LoadBalancerClient
 * @param serviceInstance 处理请求的目标实例
 */
class ServiceRequestWrapper(
    private val request: ClientHttpRequest,
    private val loadBalancerClient: LoadBalancerClient,
    private val serviceInstance: ServiceInstance
) : ClientHttpRequest {
    override fun getHeaders() = request.getHeaders()
    override fun getBody() = request.getBody()
    override fun getMethod() = request.getMethod()
    override fun execute() = request.execute()
    // replace old uri(serviceName) to new uri(host)
    override fun getURI() = loadBalancerClient.reconstructURI(serviceInstance, request.getURI())
}