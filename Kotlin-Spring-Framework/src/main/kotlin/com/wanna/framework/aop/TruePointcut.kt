package com.wanna.framework.aop

/**
 * 不对Method和Class去进行匹配
 */
class TruePointcut : Pointcut {

    companion object {
        @JvmField
        val INSTANCE = TruePointcut()
    }

    override fun getClassFilter(): ClassFilter {
        return ClassFilter.TRUE
    }

    override fun getMethodMatcher(): MethodMatcher {
        return MethodMatcher.TRUE
    }
}