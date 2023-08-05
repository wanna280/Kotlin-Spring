package com.wanna.cloud.netflix.ribbon

import com.netflix.client.IClient
import com.netflix.client.config.IClientConfig
import com.netflix.loadbalancer.ILoadBalancer
import com.wanna.cloud.context.named.NamedContextFactory

/**
 * 它创建了一个Child Context列表, 允许Specification在各自的Context当中去定义各自的Bean; 
 *
 * @see NamedContextFactory
 */
open class SpringClientFactory : NamedContextFactory<RibbonClientSpecification>(
    RibbonClientConfiguration::class.java,
    "ribbon",
    "ribbon.client.name"
) {

    /**
     * 从指定的childContext当中去获取Client
     *
     * @param name childContextName(serviceName)
     * @return 如果childContext当中包含了Client, 那么return; 不然return null
     */
    open fun <C : IClient<*, *>> getClient(name: String, clientClass: Class<C>): C? {
        return getInstance(name, clientClass)
    }

    /**
     * 从指定的childContext当中去获取LoadBalancer
     *
     * @param name childContextName(serviceName)
     * @return 如果childContext当中包含了LoadBalancer, 那么return; 不然return null
     */
    open fun getLoadBalancer(name: String): ILoadBalancer? {
        return getInstance(name, ILoadBalancer::class.java)
    }

    /**
     * 从知道哪个的childContext当中去获取ClientConfig
     *
     * @param name childContextName(serviceName)
     * @return 如果childContext当中包含了ClientConfig, 那么return; 不然return null
     */
    open fun getClientConfig(name: String): IClientConfig? {
        return getInstance(name, IClientConfig::class.java)
    }
}