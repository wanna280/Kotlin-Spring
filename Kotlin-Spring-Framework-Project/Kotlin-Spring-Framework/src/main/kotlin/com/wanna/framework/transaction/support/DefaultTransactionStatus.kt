package com.wanna.framework.transaction.support

open class DefaultTransactionStatus(
    private val transaction: Any?,
    private val newTransaction: Boolean,
    private val readOnly: Boolean,
    private val suspendedResources: Any?
) :
    AbstractTransactionStatus() {

    open fun getSuspendedResources(): Any? = this.suspendedResources

    open fun getTransaction(): Any = this.transaction ?: throw IllegalStateException("无法获取到事务")

    open fun isReadOnly(): Boolean = readOnly

    override fun isNewTransaction(): Boolean = newTransaction
}