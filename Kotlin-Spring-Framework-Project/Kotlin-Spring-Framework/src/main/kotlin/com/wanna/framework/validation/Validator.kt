package com.wanna.framework.validation

/**
 * Spring的Validator，用来完成参数的检验，是一个策略接口
 */
interface Validator {

    /**
     * 是否支持这样的JavaBean的类型的参数检验？
     *
     * @param clazz JavaBean的类型
     */
    fun supports(clazz: Class<*>) : Boolean

    /**
     * 对目标对象去进行参数检验
     *
     * @param target 要去进行参数检验的目标对象
     * @param errors errors信息
     */
    fun validate(target: Any, errors: Errors)
}