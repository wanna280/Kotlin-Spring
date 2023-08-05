package com.wanna.framework.aop.intercept

/**
 * 这是一个Invocation, 支持获取参数列表, 它本身也是一个Joinpoint
 * Invocation包括[MethodInvocation]和[ConstructorInvocation]这两类
 *
 * @see MethodInvocation
 * @see ConstructorInvocation
 */
interface Invocation : JoinPoint {

    /**
     * 获取到正在进行执行的连接点的方法参数
     *
     * @return 方法参数列表
     */
    fun getArguments(): Array<Any?>?
}