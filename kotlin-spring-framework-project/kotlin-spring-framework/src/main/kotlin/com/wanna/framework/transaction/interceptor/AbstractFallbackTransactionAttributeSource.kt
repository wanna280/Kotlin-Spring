package com.wanna.framework.transaction.interceptor

import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 提供了支持fallback的方式去进行获取@Transactional注解的方式的模板方法；
 *
 * * 1.首先会尝试去指定的方法上去找@Transactional
 * * 2.尝试去指定的方法所在的类上去找@Transactional
 *
 * @see findTransactionAttribute
 * @see TransactionAttribute
 * @see TransactionAttributeSource
 */
abstract class AbstractFallbackTransactionAttributeSource : TransactionAttributeSource {

    /**
     * 给定目标类/目标方法，尝试去匹配@Transactional注解，并封装成为TransactionAttribute
     *
     * @param method 目标方法
     * @param targetClass 目标类
     * @return 如果目标类上没有@Transactional，那么return null；如果找到了的话，将@Transactional封装成为TransactionAttribute
     */
    override fun getTransactionAttribute(method: Method, targetClass: Class<*>?): TransactionAttribute? {
        // 如果必须是public方法，但是给定的方法不是的话，那么return null
        if (allowPublicMethodsOnly() && !Modifier.isPublic(method.modifiers)) {
            return null
        }
        var txAttr: TransactionAttribute? = findTransactionAttribute(method)
        if (txAttr != null) {
            return txAttr
        }
        txAttr = findTransactionAttribute(method.declaringClass)
        if (txAttr != null) {
            return txAttr
        }
        return null
    }

    /**
     * 尝试从类上寻找@Transactional注解，封装成为TransactionAttribute
     *
     * @param targetClass 目标类
     * @return 从@Transactional解析到的TransactionAttribute
     */
    protected abstract fun findTransactionAttribute(targetClass: Class<*>): TransactionAttribute?

    /**
     * 尝试从方法上寻找@Transactional注解，封装成为TransactionAttribute
     *
     * @param method 目标方法
     * @return 从@Transactional解析到的TransactionAttribute
     */
    protected abstract fun findTransactionAttribute(method: Method): TransactionAttribute?

    /**
     * 是否只允许public方法(默认为false)拥有事务？
     *
     * @return 如果只允许public方法，return true；不然为false
     */
    protected open fun allowPublicMethodsOnly() = false
}