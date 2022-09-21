package com.wanna.boot.autoconfigure.validation

import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Lazy
import com.wanna.framework.context.annotation.Role
import com.wanna.framework.validation.beanvalidation.LocalValidatorFactoryBean
import com.wanna.framework.validation.beanvalidation.MethodValidationPostProcessor
import javax.validation.Validation
import javax.validation.Validator


/**
 * Bean Validation的自动配置类，提供对于JSR303的参数校验的支持
 */
@ConditionalOnClass(name = ["javax.validation.Validator"])
@Configuration(proxyBeanMethods = false)
open class ValidationAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean([Validator::class])
    open fun defaultValidator(): LocalValidatorFactoryBean {
        val localValidatorFactoryBean = LocalValidatorFactoryBean()
        localValidatorFactoryBean.setTargetValidator(Validation.buildDefaultValidatorFactory().validator)
        return localValidatorFactoryBean
    }

    @Bean
    @ConditionalOnClass(name = ["javax.validation.Validator"])
    @ConditionalOnMissingBean
    open fun methodValidationPostProcessor(@Lazy validator: Validator): MethodValidationPostProcessor {
        val methodValidationPostProcessor = MethodValidationPostProcessor()
        methodValidationPostProcessor.setValidator(validator)
        return methodValidationPostProcessor
    }
}