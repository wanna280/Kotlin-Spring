package com.wanna.framework.transaction

/**
 * 维护一个事务的一些属性信息, 比如超时时间、是否只读, 隔离级别、传播行为
 */
interface TransactionDefinition {
    companion object {
        // 事务的传播属性常量
        const val PROPAGATION_REQUIRED = 0
        const val PROPAGATION_SUPPORTS = 1
        const val PROPAGATION_MANDATORY = 2
        const val PROPAGATION_REQUIRES_NEW = 3
        const val PROPAGATION_NOT_SUPPORTED = 4
        const val PROPAGATION_NEVER = 5
        const val PROPAGATION_NESTED = 6

        // 事务的隔离级别常量
        const val ISOLATION_DEFAULT = -1
        const val ISOLATION_READ_UNCOMMITTED = 1
        const val ISOLATION_READ_COMMITTED = 2
        const val ISOLATION_REPEATABLE_READ = 4
        const val ISOLATION_SERIALIZABLE = 8

        // 默认的超时时间
        const val TIMEOUT_DEFAULT = -1

        /**
         * 默认的TransactionDefinition
         */
        fun withDefault(): TransactionDefinition = StaticTransactionDefinition

        object StaticTransactionDefinition : TransactionDefinition
    }

    fun getPropagationBehavior(): Int = PROPAGATION_REQUIRES_NEW

    fun getIsolationLevel(): Int = ISOLATION_DEFAULT

    fun getTimeout(): Int = TIMEOUT_DEFAULT

    fun isReadOnly(): Boolean = false

    fun getName(): String? = null
}