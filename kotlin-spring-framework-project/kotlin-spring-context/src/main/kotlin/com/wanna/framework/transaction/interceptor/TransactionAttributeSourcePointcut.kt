package com.wanna.framework.transaction.interceptor

import com.wanna.framework.aop.ClassFilter
import com.wanna.framework.aop.support.StaticMethodMatcherPointcut
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.transaction.TransactionManager
import java.lang.reflect.Method

/**
 * 事务属性的Pointcut
 */
abstract class TransactionAttributeSourcePointcut : StaticMethodMatcherPointcut() {

    init {
        this.setClassFilter(TransactionAttributeSourceClassFilter())
    }

    /**
     * 提供TransactionAttributeSource, 去对事务方法去进行匹配, 交给子类去进行实现
     *
     * @see TransactionAttributeSource
     */
    abstract fun getTransactionAttributeSource(): TransactionAttributeSource?

    /**
     * 匹配目标方法, 只要目标方法/类上有@Transactional注解就算匹配
     *
     * @param method 目标方法
     * @param targetClass 目标类
     * @return 该方法是否匹配?
     */
    override fun matches(method: Method, targetClass: Class<*>): Boolean {
        val attribute = getTransactionAttributeSource()?.getTransactionAttribute(method, targetClass)
        return attribute != null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionAttributeSourcePointcut) return false
        return getTransactionAttributeSource() == other.getTransactionAttributeSource()
    }

    override fun hashCode(): Int = TransactionAttributeSourcePointcut::class.java.hashCode()

    /**
     * 事务属性的ClassFilter, 匹配类是否符合一个注解类的要求, 委托TransactionAttributeSource去进行完成
     */
    private inner class TransactionAttributeSourceClassFilter : ClassFilter {
        override fun matches(clazz: Class<*>): Boolean {
            if (ClassUtils.isAssignFrom(TransactionManager::class.java, clazz)) {
                return false
            }
            return getTransactionAttributeSource()?.isCandidateClass(clazz) ?: true
        }
    }
}