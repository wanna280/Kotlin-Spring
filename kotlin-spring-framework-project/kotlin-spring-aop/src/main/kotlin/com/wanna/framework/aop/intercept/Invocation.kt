package com.wanna.framework.aop.intercept

/**
 * 这是一个Invocation, 支持获取参数列表, 它本身也是一个Joinpoint
 * Invocation包括MethodInvocation和ConstructorInvocation
 */
interface Invocation : JoinPoint {
    fun getArguments(): Array<Any?>?
}