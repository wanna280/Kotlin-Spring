package com.wanna.cloud.openfeign

import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingClass
import com.wanna.cloud.openfeign.support.SpringDecoder
import com.wanna.cloud.openfeign.support.SpringEncoder
import com.wanna.cloud.openfeign.support.SpringMvcContract
import com.wanna.framework.beans.factory.annotation.Qualifier
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
import feign.Retryer
import feign.codec.Decoder
import feign.codec.Encoder

/**
 * 这是针对于每个FeignClient的配置类, 会被FeignContext导入到每一个childContext当中
 */
@Configuration(proxyBeanMethods = false)
open class FeignClientsConfiguration {

    @Bean
    @Qualifier("feignConversionService")
    open fun feignConversionService(): FormattingConversionService {
        return DefaultFormattingConversionService()
    }


    @Bean
    @ConditionalOnMissingBean
    open fun feignRetryer(): Retryer {
        return Retryer.NEVER_RETRY
    }

    @Bean
    @ConditionalOnMissingBean
    open fun feignSpringDecoder(@Autowired(required = false) messageConverters: List<HttpMessageConverter<*>>): Decoder {
        return SpringDecoder(messageConverters)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun feignSpringEncoder(@Autowired(required = false) messageConverters: List<HttpMessageConverter<*>>): Encoder {
        return SpringEncoder(messageConverters)
    }

    /**
     * 它主要用来处理注解等参数的情况
     */
    @Bean
    @ConditionalOnMissingBean
    open fun springMvcContract(
        @Autowired(required = false) processors: MutableList<AnnotatedParameterProcessor>,
        @Qualifier("feignConversionService") feignConversionService: FormattingConversionService
    ): Contract {
        return SpringMvcContract(processors, feignConversionService)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value = [com.fasterxml.jackson.databind.ObjectMapper::class])
    open fun messageConverters(): HttpMessageConverter<*> {
        return MappingJackson2HttpMessageConverter()
    }

    @ConditionalOnClass(value = [com.wanna.cloud.client.circuitbreaker.CircuitBreaker::class])
    @Configuration(proxyBeanMethods = false)
    open class CircuitBreakerPresentFeignBuilderConfiguration {

        @Bean
        @Scope(BeanDefinition.SCOPE_PROTOTYPE)
        fun circuitBreakerFeignBuilder(): Feign.Builder {
            return FeignCircuitBreaker.builder()
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingClass(["com.wanna.cloud.client.circuitbreaker.CircuitBreaker"])
    open class DefaultFeignBuilderConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @Scope(BeanDefinition.SCOPE_PROTOTYPE)
        open fun feignBuilder(retryer: Retryer): Feign.Builder {
            return Feign.builder().retryer(retryer)
        }
    }
}