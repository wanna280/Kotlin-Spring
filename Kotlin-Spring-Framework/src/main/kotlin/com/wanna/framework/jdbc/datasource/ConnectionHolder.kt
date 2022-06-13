package com.wanna.framework.jdbc.datasource

import com.wanna.framework.transaction.support.ResourceHolderSupport
import java.sql.Connection


/**
 * ConnectionHolder，包装了一个Jdbc的Connection
 *
 * @param connection connection
 */
open class ConnectionHolder(val connection: Connection) : ResourceHolderSupport() {

    // 当前事务是否是活跃的
    private var transactionActive = false

    open fun setTransactionActive(transactionActive: Boolean) {
        this.transactionActive = transactionActive
    }

    open fun isTransactionActive(): Boolean = this.transactionActive
}