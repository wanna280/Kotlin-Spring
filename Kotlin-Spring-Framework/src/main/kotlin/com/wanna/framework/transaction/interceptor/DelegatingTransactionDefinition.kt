package com.wanna.framework.transaction.interceptor

import com.wanna.framework.transaction.TransactionDefinition

/**
 * TransactionDefinition的委托类，方便使用者只取重写某些方法
 */
abstract class DelegatingTransactionDefinition(private val definition: TransactionDefinition) : TransactionDefinition {
    override fun getPropagationBehavior() = definition.getPropagationBehavior()
    override fun getIsolationLevel() = definition.getIsolationLevel()
    override fun getTimeout() = definition.getTimeout()
    override fun isReadOnly() = definition.isReadOnly()
    override fun getName() = definition.getName()
}