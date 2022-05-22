package com.wanna.cloud.openfeign.ribbon

import com.wanna.boot.autoconfigure.condition.ConditionOnClass
import com.wanna.boot.autoconfigure.condition.ConditionOnMissingBean
import com.wanna.cloud.netflix.ribbon.SpringClientFactory
import com.wanna.cloud.openfeign.clientconfig.HttpClientFeignConfiguration
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import
import feign.Client
import feign.httpclient.ApacheHttpClient
import org.apache.http.client.HttpClient

/**
 * 基于Apache的HttpClient的Feign的负载均衡的配置，当Apache的HttpClient存在的情况下，将会使用Apache的HttpClient去进行网络请求的发送
 */
@ConditionOnClass(name = ["feign.httpclient.ApacheHttpClient"])
@Import([HttpClientFeignConfiguration::class])  // 导入HttpClient的配置类
@Configuration(proxyBeanMethods = false)
open class HttpClientFeignLoadBalancedConfiguration {
    @Bean
    @ConditionOnMissingBean
    open fun loadBalancerFeignClient(springClientFactory: SpringClientFactory, httpClient: HttpClient): Client {
        val delegate = ApacheHttpClient(httpClient)
        return RibbonLoadBalancerFeignClient(springClientFactory, delegate)
    }
}