package com.wanna.framework.transaction.annotation

import com.wanna.framework.transaction.TransactionDefinition

/**
 * 事务的隔离级别的枚举，每个隔离级别会对应一个int类型的level
 *
 * @see TransactionDefinition
 */
enum class Isolation(val level: Int) {
    DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),
    READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),
    READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),
    REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),
    SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE)
}