package com.wanna.framework.transaction.config

/**
 * 维护Spring事务相关的常量beanName
 */
object TransactionManagementConfigUtils {
    const val TRANSACTION_ADVISOR_BEAN_NAME = "com.wanna.framework.transaction.config.internalTransactionAdvisor"
    const val TRANSACTION_ASPECT_BEAN_NAME = "com.wanna.framework.transaction.config.internalTransactionAspect"
}