package com.wanna.cloud.openfeign

import com.wanna.boot.autoconfigure.AutoConfigureAfter
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingClass
import com.wanna.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * FeignClient的自动配置类, 它必须在FeignRibbonClientAutoConfiguration导入完成之后才去进行导入！
 */
@AutoConfigureAfter([FeignRibbonClientAutoConfiguration::class])
@Configuration(proxyBeanMethods = false)
open class FeignAutoConfiguration {

    @Autowired(required = false)
    private var configurations: List<FeignClientSpecification> = emptyList()

    /**
     * 给Spring容器当中去导入一个FeignContext, 提供childContext的创建和保存工作
     */
    @Bean
    open fun feignContext(): FeignContext {
        val feignContext = FeignContext()
        feignContext.setConfigurations(configurations)  // setConfigurations
        return feignContext
    }

    /**
     * 如果存在有CircuitBreaker的话, 那么使用SpringCloud的CircuitBreaker
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(value = [com.wanna.cloud.client.circuitbreaker.CircuitBreaker::class])
    open class CircuitBreakerPresentFeignTargeterConfiguration {
        @Bean
        @ConditionalOnMissingBean
        open fun feignCircuitBreakerTargeter(): FeignCircuitBreakerTargeter {
            return FeignCircuitBreakerTargeter()
        }
    }

    /**
     * 如果没有别的, 那么才需要导入默认的Targeter
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingClass(["com.wanna.cloud.client.circuitbreaker.CircuitBreaker"])
    open class DefaultFeignTargeterConfiguration {
        @Bean
        @ConditionalOnMissingBean
        open fun targeter(): Targeter {
            return DefaultTargeter()
        }
    }
}