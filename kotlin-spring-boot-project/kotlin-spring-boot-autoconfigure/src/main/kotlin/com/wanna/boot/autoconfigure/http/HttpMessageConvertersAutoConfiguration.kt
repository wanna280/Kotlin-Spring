package com.wanna.boot.autoconfigure.http

import com.wanna.boot.autoconfigure.AutoConfigureAfter
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.gson.GsonAutoConfiguration
import com.wanna.boot.autoconfigure.jackson.JacksonAutoConfiguration
import com.wanna.boot.autoconfigure.jsonb.JsonbAutoConfiguration
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import
import com.wanna.framework.web.http.converter.HttpMessageConverter

/**
 * [HttpMessageConverters]的自动配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
@AutoConfigureAfter([JacksonAutoConfiguration::class, GsonAutoConfiguration::class, JsonbAutoConfiguration::class])
@Import([JacksonHttpMessageConvertersConfiguration::class, GsonHttpMessageConvertersConfiguration::class, JsonbHttpMessageConvertersConfiguration::class])
@Configuration(proxyBeanMethods = false)
open class HttpMessageConvertersAutoConfiguration {
    companion object {
        /**
         * 偏好于使用哪个Json的Mapper的属性Key?
         */
        const val PREFERRED_MAPPER_PROPERTY = "spring.mvc.converters.preferred-json-mapper"
    }

    /**
     * 给SpringBeanFactory当中去导入一个[HttpMessageConverters], 去聚合所有的[HttpMessageConverter]
     *
     * @param converters BeanFactory当中的所有的[HttpMessageConverter]
     * @return HttpMessageConverters
     */
    @Bean
    @ConditionalOnMissingBean
    open fun httpMessageConverters(@Autowired(required = false) converters: List<HttpMessageConverter<*>>): HttpMessageConverters {
        return HttpMessageConverters(converters)
    }
}