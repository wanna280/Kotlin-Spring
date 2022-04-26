package com.wanna.framework.aop.framework

import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.aop.intercept.MethodInvocation
import java.lang.reflect.Method

class DefaultAdvisorChainFactory : AdvisorChainFactory {
    override fun getInterceptorsAndDynamicInterceptionAdvice(
        advised: Advised,
        method: Method,
        targetClass: Class<*>?
    ): List<Any> {
        val interceptors: ArrayList<MethodInterceptor> = ArrayList()
        interceptors.add(object : MethodInterceptor {
            override fun invoke(invocation: MethodInvocation): Any? {
                println("before")
                val returnVal = invocation.proceed()
                println("after")
                return returnVal
            }
        })
        return interceptors
    }
}