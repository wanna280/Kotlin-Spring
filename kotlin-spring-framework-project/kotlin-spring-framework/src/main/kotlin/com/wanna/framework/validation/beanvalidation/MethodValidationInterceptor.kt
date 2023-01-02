package com.wanna.framework.validation.beanvalidation

import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.aop.intercept.MethodInvocation
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.validation.annotation.Validated
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validator

/**
 * 方法的Validation的MethodInterceptor，负责使用AOP切面的方式去实现对参数的检验
 *
 * @see MethodInterceptor
 * @see MethodValidationPostProcessor
 *
 * @param validator 要使用的完成参数检验的Validator
 */
open class MethodValidationInterceptor(private val validator: Validator? = null) : MethodInterceptor {

    /**
     * 拦截目标方法，应该执行的增强的Callback逻辑
     *
     * @param invocation MethodInvocation
     * @return 执行目标方法的返回值
     * @throws Throwable 执行目标方法抛出的异常
     */
    @Throws(Throwable::class)
    override fun invoke(invocation: MethodInvocation): Any? {
        val validator = this.validator ?: throw IllegalStateException("Validator不能为空")
        // Bean Validation 1.1的标准API，提供对方法参数以及方法返回值的参数检验
        val executableValidator = validator.forExecutables()

        val validationGroups = determineValidationGroups(invocation)
        val target = invocation.getThis()
        val methodToValidate = invocation.getMethod()

        // 交给ExecutableValidator去检验目标方法当中的参数列表
        var result: Set<ConstraintViolation<Any>> = executableValidator.validateParameters(
            target, methodToValidate, invocation.getArguments(), *validationGroups
        )
        if (result.isNotEmpty()) {
            throw ConstraintViolationException(result)
        }
        // proceed，放行目标方法
        val returnValue = invocation.proceed()

        // 检验方法的返回值
        result = executableValidator.validateReturnValue(target, methodToValidate, returnValue, *validationGroups)
        if (result.isNotEmpty()) {
            throw ConstraintViolationException(result)
        }
        return returnValue
    }

    /**
     * 从`@Validated`注解的`value`属性当中去决定`ValidationGroups`
     *
     * @param invocation MethodInvocation
     * @return ValidationGroups
     */
    protected open fun determineValidationGroups(invocation: MethodInvocation): Array<Class<*>> {
        // 1.尝试从目标方法上去进行寻找
        var validated =
            AnnotatedElementUtils.getMergedAnnotationAttributes(invocation.getMethod(), Validated::class.java)
        if (validated == null) {
            val target = invocation.getThis() ?: throw IllegalStateException("target cannot be null")
            // 2.尝试从目标类上去进行寻找
            validated = AnnotatedElementUtils.getMergedAnnotationAttributes(target::class.java, Validated::class.java)
        }
        return validated?.getClassArray(MergedAnnotation.VALUE) ?: emptyArray()
    }
}