package com.wanna.framework.transaction.annotation

import com.wanna.framework.transaction.TransactionManager

/**
 * Spring的事务同步管理器的配置器，负责去设置Spring事务的事务同步管理器
 */
interface TransactionManagementConfigurer {
    fun annotationDrivenTransactionManager(): TransactionManager
}