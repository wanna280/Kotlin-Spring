package com.wanna.cloud.netflix.ribbon

import com.wanna.cloud.client.ServiceInstance
import com.wanna.cloud.client.loadbalancer.LoadBalancerClient
import com.wanna.cloud.client.loadbalancer.LoadBalancerRequest

/**
 * Ribbon针对于SpringCloud的实现的LoadBalancerClient，它组合了SpringClientFactory，支持从childContext当中，去获取组件
 *
 * @param springClientFactory SpringClientFactory(NamedContextFactory)
 */
open class RibbonLoadBalancerClient(private val springClientFactory: SpringClientFactory) : LoadBalancerClient {

    override fun <T> execute(serviceId: String, request: LoadBalancerRequest<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T> execute(serviceId: String, serviceInstance: ServiceInstance, request: LoadBalancerRequest<T>): T {
        TODO("Not yet implemented")
    }

    override fun choose(serviceId: String): ServiceInstance? {
        val loadBalancer = springClientFactory.getLoadBalancer(serviceId)!!
        val server = loadBalancer.chooseServer(serviceId)

        println("choose Server ... $server")
        return null
    }
}