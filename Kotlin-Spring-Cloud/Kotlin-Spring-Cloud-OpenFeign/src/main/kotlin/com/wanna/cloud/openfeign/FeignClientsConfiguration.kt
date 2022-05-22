package com.wanna.cloud.openfeign

import com.wanna.boot.autoconfigure.condition.ConditionOnMissingBean
import com.wanna.cloud.openfeign.support.SpringDecoder
import com.wanna.cloud.openfeign.support.SpringEncoder
import com.wanna.cloud.openfeign.support.SpringMvcContract
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Scope
import com.wanna.framework.context.format.support.DefaultFormattingConversionService
import com.wanna.framework.context.format.support.FormattingConversionService
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.http.converter.json.MappingJackson2HttpMessageConverter
import feign.Contract
import feign.Feign
import feign.codec.Decoder
import feign.codec.Encoder

/**
 * 这是针对于每个FeignClient的配置类，会被FeignContext导入到每一个childContext当中
 */
@Configuration(proxyBeanMethods = false)
open class FeignClientsConfiguration {

    @Bean
    open fun formattingConversionService(): FormattingConversionService {
        return DefaultFormattingConversionService()
    }

    @Bean
    @ConditionOnMissingBean
    @Scope(BeanDefinition.SCOPE_PRTOTYPE)
    open fun feign(): Feign.Builder {
        return Feign.builder()
    }

    @Bean
    @ConditionOnMissingBean
    open fun feignSpringDecoder(@Autowired(required = false) messageConverters: List<HttpMessageConverter<*>>): Decoder {
        return SpringDecoder(messageConverters)
    }

    @Bean
    @ConditionOnMissingBean
    open fun feignSpringEncoder(@Autowired(required = false) messageConverters: List<HttpMessageConverter<*>>): Encoder {
        return SpringEncoder(messageConverters)
    }

    /**
     * 它主要用来处理注解等参数的情况
     */
    @Bean
    @ConditionOnMissingBean
    open fun springMvcContract(
        @Autowired(required = false) processors: MutableList<AnnotatedParameterProcessor>,
        conversionService: FormattingConversionService
    ): Contract {
        return SpringMvcContract(processors, conversionService)
    }

    @Bean
    @ConditionOnMissingBean
    open fun messageConverters(): HttpMessageConverter<*> {
        return MappingJackson2HttpMessageConverter()
    }
}