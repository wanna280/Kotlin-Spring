package com.wanna.framework.transaction.interceptor

/**
 * 委托的TransactionAttribute，方便使用者只取重写某些方法
 *
 * @param txAttr 受委托的TransactionAttribute
 */
abstract class DelegatingTransactionAttribute(private val txAttr: TransactionAttribute) : TransactionAttribute,
    DelegatingTransactionDefinition(txAttr) {
    override fun getQualifier() = txAttr.getQualifier()
    override fun rollbackOn(ex: Throwable) = txAttr.rollbackOn(ex)
}