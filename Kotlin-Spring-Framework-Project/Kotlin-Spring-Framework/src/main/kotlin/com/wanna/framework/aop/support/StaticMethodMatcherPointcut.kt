package com.wanna.framework.aop.support

import com.wanna.framework.aop.ClassFilter
import com.wanna.framework.aop.Pointcut

/**
 * 静态方法的匹配的Pointcut，默认的ClassFilter的实现为直接放行
 *
 * @see StaticMethodMatcher
 */
abstract class StaticMethodMatcherPointcut : Pointcut, StaticMethodMatcher() {
    private var classFilter: ClassFilter = ClassFilter.TRUE

    open fun setClassFilter(classFilter: ClassFilter) {
        this.classFilter = classFilter
    }
    override fun getClassFilter() = classFilter
    override fun getMethodMatcher() = this
}