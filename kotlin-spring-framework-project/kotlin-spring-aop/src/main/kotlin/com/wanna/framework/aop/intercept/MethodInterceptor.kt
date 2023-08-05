package com.wanna.framework.aop.intercept

/**
 * 标识这是一个MethodInterceptor, 拦截目标方法的执行
 *
 * @see com.wanna.framework.aop.Advice
 * @author jianchao.jia
 */
interface MethodInterceptor : Interceptor {

    /**
     * 拦截目标方法执行的拦截器的Callback方法, 如果需要放行到下一个MethodInterceptor,
     * 可以使用[MethodInvocation.proceed]这个方法去进行放行
     *
     * @param invocation 方法执行的上下文
     * @return 执行目标方法的上下文
     */
    fun invoke(invocation: MethodInvocation): Any?
}