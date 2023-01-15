package com.wanna.cloud.openfeign.ribbon

import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import

/**
 * Feign的RibbonClient的自动配置类, 为Spring容器当中导入RibbonClient相关的配置, 利用Ribbon去提供负载均衡;
 * 它负责给容器当中导入对应类型的LoadBalancedConfiguration, 但是导入的顺序很关键, 一定要按照使用的优先级去进行配置;
 * 如果直接通过自动配置类去进行配置, 则比较复杂, 我们在这里通过@Import注解, @Import会自动地去进行按照顺序去进行导入！
 *
 * @see DefaultFeignLoadBalancedConfiguration
 * @see RibbonLoadBalancerFeignClient
 */
@ConditionalOnClass(value = [com.netflix.loadbalancer.ILoadBalancer::class, feign.Feign::class])
// import order is very import!!!default must be the last
@Import([HttpClientFeignLoadBalancedConfiguration::class, DefaultFeignLoadBalancedConfiguration::class])
@Configuration(proxyBeanMethods = false)
open class FeignRibbonClientAutoConfiguration