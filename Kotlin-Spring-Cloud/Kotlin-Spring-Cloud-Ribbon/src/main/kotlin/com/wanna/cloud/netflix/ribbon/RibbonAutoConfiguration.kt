package com.wanna.cloud.netflix.ribbon

import com.wanna.boot.autoconfigure.condition.ConditionOnMissingBean
import com.wanna.cloud.client.loadbalancer.LoadBalancerClient
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * Ribbon的自动配置类，需要导入到App容器当中，去完成自动配置
 */
@Configuration(proxyBeanMethods = false)
open class RibbonAutoConfiguration {

    /**
     * 从App容器当中去注入所有的Specification，支持去对SpringClientFactory当中的所有childContext去进行配置
     */
    @Autowired(required = false)
    private var specifications: List<RibbonClientSpecification> = emptyList()

    /**
     * 给App容器当中去导入一个SpringClientFactory，支持Ribbon从SpringClientFactory当中去获取各种各样的组件，去完成负载均衡的实现；
     */
    @Bean
    @ConditionOnMissingBean
    open fun springClientFactory(): SpringClientFactory {
        val springClientFactory = SpringClientFactory()
        springClientFactory.setConfigurations(specifications)
        return springClientFactory
    }

    /**
     * 给App容器当中去导入一个LoadBalancerClient，它是所有的SpringCloud的负载均衡的入口
     *
     * @param springClientFactory SpringClientFactory
     */
    @Bean
    @ConditionOnMissingBean
    open fun loadBalancerClient(springClientFactory: SpringClientFactory) : LoadBalancerClient {
        return RibbonLoadBalancerClient(springClientFactory)
    }

}