package com.wanna.framework.aop.framework

import com.wanna.framework.aop.PointcutAdvisor
import com.wanna.framework.aop.intercept.MethodInterceptor
import java.lang.reflect.Method

open class DefaultAdvisorChainFactory : AdvisorChainFactory {
    override fun getInterceptorsAndDynamicInterceptionAdvice(
        advised: Advised,
        method: Method,
        targetClass: Class<*>?
    ): List<Any> {
        val methodInterceptors = ArrayList<MethodInterceptor>()
        if (targetClass == null) {
            return methodInterceptors
        }
        val advisors = advised.getAdvisors()
        advisors.forEach {
            if (it is PointcutAdvisor) {
                if (it.getPointcut().getMethodMatcher().matches(method, targetClass)) {
                    methodInterceptors.add(it.getAdvice() as MethodInterceptor)
                    return@forEach
                }
            }
        }
        return methodInterceptors
    }
}