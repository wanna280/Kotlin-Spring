package com.wanna.framework.aop

/**
 * 不对Method和Class去进行匹配的Pointcut，对于方法和类的匹配，都return TURE直接放行
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