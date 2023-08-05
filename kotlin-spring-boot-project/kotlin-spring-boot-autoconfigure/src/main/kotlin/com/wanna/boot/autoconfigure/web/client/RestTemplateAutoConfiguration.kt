package com.wanna.boot.autoconfigure.web.client

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.http.HttpMessageConverters
import com.wanna.boot.web.client.RestTemplateBuilder
import com.wanna.boot.web.client.RestTemplateCustomizer
import com.wanna.boot.web.client.RestTemplateRequestCustomizer
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Lazy
import com.wanna.framework.web.client.RestTemplate
import java.util.*

/**
 * [RestTemplate]的自动配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 */
@Configuration(proxyBeanMethods = false)
open class RestTemplateAutoConfiguration {

    /**
     * 自动注入[HttpMessageConverters], [RestTemplateRequestCustomizer]和[RestTemplateCustomizer], 去实现对于[RestTemplateBuilder]的自定义
     *
     * @param restTemplateCustomizers RestTemplate的自定义化器列表
     * @param restTemplateRequestCustomizers RestTemplate的Request的自定义话器列表
     * @param messageConverters HttpMessageConverters
     */
    @Bean
    @Lazy
    @ConditionalOnMissingBean
    open fun restTemplateBuilderConfigurer(
        @Autowired(required = false) restTemplateCustomizers: List<RestTemplateCustomizer>?,
        @Autowired(required = false) restTemplateRequestCustomizers: List<RestTemplateRequestCustomizer<*>>?,
        @Autowired(required = false) messageConverters: Optional<HttpMessageConverters>
    ): RestTemplateBuilderConfigurer {
        val configurer = RestTemplateBuilderConfigurer()
        configurer.httpMessageConverters = messageConverters.orElseGet { null }
        configurer.restTemplateRequestCustomizers = restTemplateRequestCustomizers
        configurer.restTemplateCustomizers = restTemplateCustomizers
        return configurer
    }

    /**
     * 给SpringBeanFactory当中导入[RestTemplateBuilder]
     *
     * @param configurer RestTemplateBuilder的配置器
     * @return RestTemplateBuilder
     */
    @Bean
    @Lazy
    @ConditionalOnMissingBean
    open fun restTemplateBuilder(configurer: RestTemplateBuilderConfigurer): RestTemplateBuilder {
        return configurer.configure(RestTemplateBuilder())
    }
}