package com.wanna.framework.transaction.interceptor

import com.wanna.framework.transaction.TransactionDefinition

/**
 * 事务的属性信息, 对TransactionDefinition去进行扩展
 *
 * @see TransactionDefinition
 */
interface TransactionAttribute : TransactionDefinition {

    /**
     * 获取TransactionManager的Qualifier
     *
     * @return Qualifier
     */
    fun getQualifier(): String?

    /**
     * 在给定的异常的情况下, 事务是否需要去进行回滚？
     *
     * @param ex 给定的一场
     * @return 需要回滚return true; 不然return false
     */
    fun rollbackOn(ex: Throwable): Boolean
}