package com.wanna.framework.transaction.annotation

import com.wanna.framework.transaction.TransactionDefinition

/**
 * 事务传播属性，每个传播属性会对应一个value，对应TransactionDefinition当中的常量值
 *
 * @see TransactionDefinition
 *
 * @param value propagation value
 */
enum class Propagation(val value: Int) {
    REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),
    SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),
    REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),
    MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),
    NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),
    NEVER(TransactionDefinition.PROPAGATION_NEVER),
    NESTED(TransactionDefinition.PROPAGATION_NESTED)
}