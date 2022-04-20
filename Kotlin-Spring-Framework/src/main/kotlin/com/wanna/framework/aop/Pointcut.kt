package com.wanna.framework.aop

interface Pointcut {
    fun getClassFilter(): ClassFilter

    fun getMethodMatcher(): MethodMatcher

    companion object {
        val TRUE = TruePointcut.INSTANCE
    }
}