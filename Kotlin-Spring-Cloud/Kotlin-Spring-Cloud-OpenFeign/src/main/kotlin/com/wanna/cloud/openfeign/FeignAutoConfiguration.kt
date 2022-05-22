package com.wanna.cloud.openfeign

import com.wanna.boot.autoconfigure.AutoConfigureAfter
import com.wanna.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * FeignClient的自动配置类，它必须在FeignRibbonClientAutoConfiguration导入完成之后才去进行导入！
 */
@AutoConfigureAfter([FeignRibbonClientAutoConfiguration::class])
@Configuration(proxyBeanMethods = false)
open class FeignAutoConfiguration {

    @Autowired(required = false)
    private var configurations: List<FeignClientSpecification> = emptyList()

    /**
     * 给Spring容器当中去导入一个FeignContext，提供childContext的创建和保存工作
     */
    @Bean
    open fun feignContext(): FeignContext {
        val feignContext = FeignContext()
        feignContext.setConfigurations(configurations)  // setConfigurations
        return feignContext
    }

    /**
     * 给容器中导入Targeter对象
     */
    @Bean
    open fun targeter(): Targeter {
        return DefaultTargeter()
    }

}