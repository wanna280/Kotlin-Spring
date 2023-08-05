package com.wanna.framework.aop

/**
 * SpringAop的Pointcut, 基于[ClassFilter]和[MethodMatcher], 去实现去对类和方法去进行匹配
 *
 * @see ClassFilter
 * @see MethodMatcher
 */
interface Pointcut {

    companion object {
        @JvmField
        val TRUE = TruePointcut
    }

    /**
     * 获取到对类去进行匹配的[ClassFilter]
     *
     * @return ClassFilter
     */
    fun getClassFilter(): ClassFilter

    /**
     * 获取到对方法去进行匹配的[MethodMatcher]
     *
     * @return MethodMatcher
     */
    fun getMethodMatcher(): MethodMatcher
}