package com.wanna.framework.aop.intercept

/**
 * 标识这是一个MethodInterceptor，拦截目标方法的执行
 */
interface MethodInterceptor : Interceptor {
    fun invoke(invocation: MethodInvocation): Any?
}