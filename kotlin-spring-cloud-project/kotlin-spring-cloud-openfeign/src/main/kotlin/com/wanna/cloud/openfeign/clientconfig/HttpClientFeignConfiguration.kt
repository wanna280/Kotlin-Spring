package com.wanna.cloud.openfeign.clientconfig

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

/**
 * Apache的HttpClient的自动配置类, 负责将Apache的HttpClient相关的配置;
 * 因为有可能需要在多个地方(Ribbon/LoadBalancer)去进行导入, 因此这里做成通用的, 抽离成为一个单独的配置类;
 */
@Configuration(proxyBeanMethods = false)
open class HttpClientFeignConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun httpClient(): CloseableHttpClient {
        return HttpClients.custom().build()
    }
}