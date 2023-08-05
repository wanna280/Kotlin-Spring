package com.wanna.framework.transaction.interceptor

import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.aop.intercept.MethodInvocation

/**
 * Spring事务的增强拦截器器, 负责对给容器中需要产生事务的Bean去生成代理, 而该代理, 会被当前的组件所拦截和进行处理
 *
 * @see TransactionAspectSupport
 */
open class TransactionInterceptor : MethodInterceptor, TransactionAspectSupport() {

    /**
     * 对@Transaction事务方法去进行拦截, 并在包围目标方法的情况下去执行目标事务方法
     *
     * @param invocation invocation
     * @return 要执行的目标方法的返回值
     */
    override fun invoke(invocation: MethodInvocation): Any? {
        // 构建执行目标方法的Callback, 为了包装给父类的invokeWithinTransaction方法, 去完成目标方法的执行(放行Aop的拦截链)
        val callback = object : InvocationCallback {
            override fun proceedWithInvocation(): Any? {
                return invocation.proceed()
            }
        }
        return invokeWithinTransaction(invocation.getMethod(), null, callback)
    }
}