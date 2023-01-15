package com.wanna.framework.validation.beanvalidation

import com.wanna.framework.aop.Advice
import com.wanna.framework.aop.framework.AbstractBeanFactoryAwareAdvisingPostProcessor
import com.wanna.framework.aop.support.DefaultPointcutAdvisor
import com.wanna.framework.aop.support.annotation.AnnotationMatchingPointcut
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.lang.Nullable
import com.wanna.framework.validation.annotation.Validated
import javax.validation.ValidatorFactory

/**
 * MethodValidationPostProcessor, 提供对于方法级别的方法参数的匹配; 
 * 在**类型级别**上去标注一个`@Validated`注解, 即可支持对该类当中的方法参数去进行检验; 
 * 下面是一个参数检验的案例：
 *
 * ```java
 * public @NotNull Object myValidMethod(@NotNull String arg1, @Max(10) int arg2)
 * ```
 *
 * 当然也支持JSR303当中对于Group的支持, 当然如果默认不去进行配置的话, 使用默认的Group
 *
 * @see AbstractBeanFactoryAwareAdvisingPostProcessor
 * @see MethodValidationInterceptor
 * @see javax.validation.Validator
 */
open class MethodValidationPostProcessor : AbstractBeanFactoryAwareAdvisingPostProcessor(), InitializingBean {
    /**
     * 要匹配的注解类型, 默认支持去匹配Spring家的@Validated注解(支持去进行自定义)
     */
    private var validatedAnnotationType: Class<out Annotation> = Validated::class.java

    /**
     * 内部去进行真正的参数检验时需要使用的Validator
     */
    @Nullable
    private var validator: javax.validation.Validator? = null

    /**
     * 设置要去进行匹配的注解类型, 默认情况下为Spring的`@Validated`注解
     *
     * @param validatedAnnotationType 要去进行匹配的注解类型
     */
    open fun setValidatedAnnotationType(validatedAnnotationType: Class<out Annotation>) {
        this.validatedAnnotationType = validatedAnnotationType
    }

    /**
     * set Validator
     *
     * @param validator javax.validation.Validator
     */
    open fun setValidator(validator: javax.validation.Validator) {
        if (validator is SpringValidatorAdapter) {
            this.validator = validator.getTargetValidator()
        } else {
            this.validator = validator
        }
    }

    /**
     * set ValidatorFactory, 去构建出来Validator并完成Validator的设置
     *
     * @param validatorFactory javax.validation.ValidatorFactory, 提供Validator的创建
     */
    open fun setValidatorFactory(validatorFactory: ValidatorFactory) {
        this.validator = validatorFactory.validator
    }

    /**
     * 在初始化时, 构建出来Advisor用来去匹配类上的`@Validated`注解, 使用SpringAOP去生成代理
     *
     * @see AnnotationMatchingPointcut
     */
    override fun afterPropertiesSet() {
        val pointcut = AnnotationMatchingPointcut(this.validatedAnnotationType)
        this.advisor = DefaultPointcutAdvisor(pointcut, createMethodValidationAdvice(validator))
    }

    /**
     * 创建一个针对于MethodValidation的Advice, 提供对标注了`@Validated`注解方法参数去进行检验
     *
     * @param validator Validator, 可以为空
     * @return 创建好的Advice(MethodValidationInterceptor)
     * @see MethodValidationInterceptor
     */
    protected open fun createMethodValidationAdvice(@Nullable validator: javax.validation.Validator?): Advice {
        return if (validator != null) MethodValidationInterceptor(validator) else MethodValidationInterceptor()
    }
}