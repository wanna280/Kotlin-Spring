package com.wanna.framework.aop.framework

import com.wanna.framework.aop.intercept.MethodInterceptor
import java.lang.reflect.Method

open class DefaultAdvisorChainFactory : AdvisorChainFactory {
    override fun getInterceptorsAndDynamicInterceptionAdvice(
        advised: Advised,
        method: Method,
        targetClass: Class<*>?
    ): List<Any> {
        return ArrayList<MethodInterceptor>()
    }
}