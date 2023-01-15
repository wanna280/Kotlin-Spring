package com.wanna.framework.aop

/**
 * SpringAop的Pointcut, 支持去对类和方法去进行匹配
 *
 * @see ClassFilter
 * @see MethodMatcher
 */
interface Pointcut {

    companion object {
        @JvmField
        val TRUE = TruePointcut.INSTANCE
    }

    fun getClassFilter(): ClassFilter

    fun getMethodMatcher(): MethodMatcher
}