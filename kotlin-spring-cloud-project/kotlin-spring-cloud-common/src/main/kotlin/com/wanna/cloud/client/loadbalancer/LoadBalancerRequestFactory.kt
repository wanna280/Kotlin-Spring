package com.wanna.cloud.client.loadbalancer

import com.wanna.cloud.client.ServiceInstance
import com.wanna.framework.web.http.client.ClientHttpRequest
import com.wanna.framework.web.http.client.ClientHttpRequestExecution
import com.wanna.framework.web.http.client.ClientHttpResponse

/**
 * LoadBalancer的RequestFactory
 *
 * @param loadBalancerClient LoadBalancerClient
 */
class LoadBalancerRequestFactory(private val loadBalancerClient: LoadBalancerClient) {

    /**
     * 创建LoadBalancerRequest
     *
     * @param request 原始的请求
     * @param body RequestBody
     * @param execution 请求的执行器链
     */
    fun createRequest(
        request: ClientHttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): LoadBalancerRequest<ClientHttpResponse> {
        return object : LoadBalancerRequest<ClientHttpResponse> {
            override fun apply(serviceInstance: ServiceInstance): ClientHttpResponse {
                // 包装原来的Request, 将serviceName替换成为ServiceInstance当中的真实的主机host
                val wrappedRequest = ServiceRequestWrapper(request, loadBalancerClient, serviceInstance)
                return execution.execute(wrappedRequest, body)
            }
        }
    }
}