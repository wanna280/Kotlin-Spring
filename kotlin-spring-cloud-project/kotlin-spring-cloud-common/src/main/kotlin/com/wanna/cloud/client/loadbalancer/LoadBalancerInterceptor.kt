package com.wanna.cloud.client.loadbalancer

import com.wanna.framework.web.http.client.ClientHttpRequest
import com.wanna.framework.web.http.client.ClientHttpRequestExecution
import com.wanna.framework.web.http.client.ClientHttpRequestInterceptor
import com.wanna.framework.web.http.client.ClientHttpResponse

/**
 * 为RestTemplate提供负载均衡策略的客户端Http请求的拦截器(ClientHttpRequestInterceptor)
 *
 * @param loadBalancerClient LoadBalancerClient, 可以去执行负载均衡的请求
 * @param requestFactory 创建LoadBalancerRequest的Factory(LoadBalancer执行请求时, 需要用到一个request的callback, 而这个参数就是去创建该callback)
 */
open class LoadBalancerInterceptor(
    private val loadBalancerClient: LoadBalancerClient, private val requestFactory: LoadBalancerRequestFactory
) : ClientHttpRequestInterceptor {

    override fun intercept(
        request: ClientHttpRequest, body: ByteArray, execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        // 获取原始的request当中的uri中的host, 作为serviceName, eg:"http://wanna/user", 我们要获取到的serviceName=wanna
        val serviceName = request.getURI().host.toString()
        // 使用LoadBalancer去发送请求, 第一个参数是serviceName, 第二个参数是要执行请求的方式的回调
        // 使用第二个callback回调函数, 可以execute, 获取到ClientHttpResponse, 去进行return, 作为整个方法(loadBalancerClient.execute)的返回值
        return this.loadBalancerClient.execute(serviceName, requestFactory.createRequest(request, body, execution))
    }
}