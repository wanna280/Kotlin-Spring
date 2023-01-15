package com.wanna.cloud.openfeign.ribbon

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.cloud.netflix.ribbon.SpringClientFactory
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import feign.Client

/**
 * 默认情况下的配置Feign的Ribbon负载均衡客户端的配置类; 在没有别的HttpClient存在的情况下, 使用这个Client
 */
@Configuration(proxyBeanMethods = false)
open class DefaultFeignLoadBalancedConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun loadBalancerFeignClient(springClientFactory: SpringClientFactory): Client {
        return RibbonLoadBalancerFeignClient(springClientFactory, Client.Default(null, null))
    }
}