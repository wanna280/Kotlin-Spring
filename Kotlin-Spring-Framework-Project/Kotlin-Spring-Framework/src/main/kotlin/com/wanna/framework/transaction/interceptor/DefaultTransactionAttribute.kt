package com.wanna.framework.transaction.interceptor

import com.wanna.framework.transaction.support.DefaultTransactionDefinition

/**
 * 事务的属性信息的默认具体实现
 *
 * @see DefaultTransactionDefinition
 * @see TransactionAttribute
 */
open class DefaultTransactionAttribute : TransactionAttribute, DefaultTransactionDefinition() {
    // 事务同步管理器的Qualifier(beanName)
    private var qualifier: String? = null

    final override fun getQualifier() = qualifier

    fun setQualifier(qualifier: String?) {
        this.qualifier = qualifier
    }

    /**
     * 默认的回滚条件是，当异常是运行时异常，或者是Error的情况下去进行回滚，允许子类去进行扩展
     *
     * @param ex 要去进行匹配是否需要回滚事务的异常
     * @return 如果该异常是RuntimeException/Error时需要去进行回滚 return true；否则return false
     */
    override fun rollbackOn(ex: Throwable) = ex is RuntimeException || ex is java.lang.Error
}