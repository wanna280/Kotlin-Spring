package com.wanna.framework.validation

/**
 * 智能的Validator, 是Validator的变体, 新增validationHints的支持
 *
 * @see Validator
 */
interface SmartValidator : Validator {

    /**
     * 对`target`目标对象去进行参数检验, `Errors`用于去报告相关的错误信息, `validationHints`则主要是提供
     * validationHints的支持, 比如JSR303的Group(在这种情况下, validationHints的类型必须是Class),
     * Note: 对于validationHints很可能被Validator所忽略, 这种情况下, 这个方法的作用和不使用validationHints
     * 的作用完全相同
     *
     * @param target 待完成参数检验的目标对象
     * @param errors Errors, 用来报告参数检验过程当中发生的异常信息
     * @param validationHints validationHints
     * @see javax.validation.Validator.validate
     */
    fun validate(target: Any, errors: Errors, vararg validationHints: Any)
}