package com.wanna.cloud.client.loadbalancer

import com.wanna.cloud.client.ServiceInstance

/**
 * 它是一个LoadBalancer的Request, 为了实现LoadBalancerClient, 必须要进行的一层抽象
 */
interface LoadBalancerRequest<T> {
    /**
     * 给定一个ServiceInstance, 供实现方对它进行apply(比如发送网络请求)
     *
     * @param serviceInstance ServiceInstance
     * @return apply ServiceInstance的返回结果(比如说网络请求的Response)
     */
    fun apply(serviceInstance: ServiceInstance): T
}