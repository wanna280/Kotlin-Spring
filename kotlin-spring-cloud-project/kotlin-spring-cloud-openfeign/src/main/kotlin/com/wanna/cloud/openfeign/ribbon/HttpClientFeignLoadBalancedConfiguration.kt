package com.wanna.cloud.openfeign.ribbon

import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.cloud.netflix.ribbon.SpringClientFactory
import com.wanna.cloud.openfeign.clientconfig.HttpClientFeignConfiguration
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import
import feign.Client
import feign.httpclient.ApacheHttpClient
import org.apache.http.client.HttpClient

/**
 * 基于Apache的HttpClient的Feign的负载均衡的配置, 当Apache的HttpClient存在的情况下, 将会使用Apache的HttpClient去进行网络请求的发送
 */
@ConditionalOnClass(value = [feign.httpclient.ApacheHttpClient::class])
@Import([HttpClientFeignConfiguration::class])  // 导入HttpClient的配置类
@Configuration(proxyBeanMethods = false)
open class HttpClientFeignLoadBalancedConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun loadBalancerFeignClient(springClientFactory: SpringClientFactory, httpClient: HttpClient): Client {
        return RibbonLoadBalancerFeignClient(springClientFactory, ApacheHttpClient(httpClient))
    }
}