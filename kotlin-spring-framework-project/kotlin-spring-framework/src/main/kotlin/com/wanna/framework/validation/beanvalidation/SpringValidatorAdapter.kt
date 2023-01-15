package com.wanna.framework.validation.beanvalidation

import com.wanna.framework.lang.Nullable
import com.wanna.framework.validation.*
import java.util.*
import javax.validation.ConstraintViolation
import javax.validation.ElementKind
import javax.validation.executable.ExecutableValidator
import javax.validation.metadata.BeanDescriptor
import javax.validation.metadata.ConstraintDescriptor

/**
 * ValidatorAdapter, 可以将`javax.validation.Validator`转换到Spring的Validator,
 * 当然也可以使用`javax.validation.Validator`自身的方式去进行对外暴露.
 *
 * 可以基于编程式, 去设置内部的target Validator作为Wrapper; 当然也可以使用`CustomValidatorBean`和
 * `LocalValidatorFactoryBean`去作为`SmartValidator`的主要的实现
 *
 * @see javax.validation.Validator
 * @param targetValidator 用来进行最终的参数检验的目标JSR303的Validator
 */
open class SpringValidatorAdapter(@Nullable private var targetValidator: javax.validation.Validator? = null) :
    SmartValidator, javax.validation.Validator {
    companion object {
        /**
         * BeanValidation相关注解的内部属性信息, 对于每个Validation相关的注解都会应该去存在这些属性
         *
         * @see javax.validation.constraints.NotBlank
         * @see javax.validation.constraints.NotEmpty
         */
        @JvmStatic
        private val internalAnnotationAttributes: MutableSet<String> = HashSet(4)

        init {
            internalAnnotationAttributes.add("message")
            internalAnnotationAttributes.add("groups")
            internalAnnotationAttributes.add("payload")
        }
    }


    /**
     * set target javax.validation.Validator
     *
     * @param validator javax.validation.Validator
     */
    open fun setTargetValidator(validator: javax.validation.Validator) {
        this.targetValidator = validator
    }

    /**
     * get target javax.validation.Validator
     *
     * @return target javax.validation.Validator
     */
    open fun getTargetValidator(): javax.validation.Validator? = this.targetValidator

    /**
     * 只要存在有targetValidator, 那么就支持去进行检验
     *
     * @param clazz 要去进行检验的目标类
     */
    override fun supports(clazz: Class<*>) = this.targetValidator != null

    /**
     * 来自Validator的validate方法, 支持使用targetValidator去对目标参数去进行检验
     *
     * @param target 待检验参数的目标对象
     * @param errors Errors, 用来记录参数检验过程当中发生的错误信息
     */
    override fun validate(target: Any, errors: Errors) {
        Optional.ofNullable(targetValidator).ifPresent {
            processConstraintViolations(it.validate(target), errors)
        }
    }

    /**
     * 来自SmartValidator的validate方法, 新增validationHints的实现,
     * 在这里会将validationHints去转换成为ValidationGroup去进行检验
     *
     * @param target 待完成参数检验的目标对象
     * @param errors 用于记录Error信息
     * @param validationHints validationHints, 将会被用做ValidationGroup
     */
    override fun validate(target: Any, errors: Errors, vararg validationHints: Any) {
        Optional.ofNullable(targetValidator).ifPresent {
            processConstraintViolations(
                this.targetValidator!!.validate(target, *asValidationGroups(*validationHints)),
                errors
            )
        }
    }

    /**
     * 来自JSR303的Validator的validate方法
     *
     * @param `object` 待去进行参数检验的目标对象
     * @param groups ValidationGroups
     * @return validate过程当中发生的违反的约束条件
     */
    override fun <T : Any?> validate(`object`: T, vararg groups: Class<*>?): MutableSet<ConstraintViolation<T>>? =
        this.targetValidator?.validate(`object`, *groups)


    /**
     * 来自JSR303的Validator的validateProperty方法
     *
     * @param `object` 待去进行参数检验的目标对象
     * @param propertyName 属性名
     * @param groups validationGroups
     */
    override fun <T : Any?> validateProperty(
        `object`: T,
        propertyName: String?,
        vararg groups: Class<*>?
    ): MutableSet<ConstraintViolation<T>>? = this.targetValidator?.validateProperty(`object`, propertyName, *groups)

    override fun <T : Any?> validateValue(
        beanType: Class<T>?,
        propertyName: String?,
        value: Any?,
        vararg groups: Class<*>?
    ): MutableSet<ConstraintViolation<T>>? = this.targetValidator?.validateValue(beanType, propertyName, value, *groups)

    override fun getConstraintsForClass(clazz: Class<*>?): BeanDescriptor? =
        this.targetValidator?.getConstraintsForClass(clazz)

    override fun <T : Any?> unwrap(type: Class<T>?): T? = this.targetValidator?.unwrap(type)

    override fun forExecutables(): ExecutableValidator? = targetValidator?.forExecutables()

    /**
     * 将validationHints转换成为ValidationGroup
     *
     * @param validationHints validationHints
     * @return Array<Class>, 从validationHints当中去过滤出来所有的Class作为Group
     */
    private fun asValidationGroups(vararg validationHints: Any): Array<Class<*>> =
        validationHints.filterIsInstance<Class<*>>().toTypedArray()

    /**
     * 处理JSR330当中Validator解析违反的约束情况的ConstraintViolation, 并将结果设置到Spring提供的实现的Errors当中
     *
     * @param violations 违反约束情况(ConstraintViolations), 由JSR303的Validator处理得到
     * @param errors Errors, 记录错误信息
     */
    protected open fun processConstraintViolations(violations: Set<ConstraintViolation<Any>>, errors: Errors) {
        violations.forEach {
            val field = determineField(it) // 决定字段名
            val fieldError = errors.getFieldError(field)  // get FieldError
            if (fieldError == null || !fieldError.isBindingFailure) {
                val errorCode = determineErrorCode(it.constraintDescriptor)
                if (errors is BindingResult) {
                    errors.addError(ViolationFieldError(this, it))
                }
            }

        }
    }

    protected open fun determineField(violation: ConstraintViolation<Any>): String {
        val path = violation.propertyPath
        val sb = StringBuilder()
        var first = true
        for (node in path) {
            if (node.isInIterable) {
                sb.append('[')
                var index: Any? = node.index
                if (index == null) {
                    index = node.key
                }
                if (index != null) {
                    sb.append(index)
                }
                sb.append(']')
            }
            val name = node.name
            if (name != null && node.kind == ElementKind.PROPERTY && !name.startsWith("<")) {
                if (!first) {
                    sb.append('.')
                }
                first = false
                sb.append(name)
            }
        }
        return sb.toString()
    }

    protected open fun determineErrorCode(descriptor: ConstraintDescriptor<*>): String {
        return descriptor.annotation::class.java.simpleName
    }

    class ViolationObjectError(val adapter: SpringValidatorAdapter, val violation: ConstraintViolation<*>) :
        ObjectError() {
        init {
            wrap(violation)
        }
    }

    class ViolationFieldError(val adapter: SpringValidatorAdapter, val violation: ConstraintViolation<*>) :
        FieldError() {
        init {
            wrap(violation)
        }
    }
}