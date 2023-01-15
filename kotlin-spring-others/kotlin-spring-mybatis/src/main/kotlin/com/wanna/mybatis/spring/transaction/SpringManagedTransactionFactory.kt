package com.wanna.mybatis.spring.transaction

import org.apache.ibatis.session.TransactionIsolationLevel
import org.apache.ibatis.transaction.Transaction
import org.apache.ibatis.transaction.TransactionFactory
import java.sql.Connection
import javax.sql.DataSource

/**
 * 在Spring的管理下的MyBatis事务
 *
 * @see SpringManagedTransaction
 * @see TransactionFactory
 */
open class SpringManagedTransactionFactory : TransactionFactory {

    /**
     * 使用Connection去创建事务, 不支持这种方式
     *
     * @param conn Connection
     */
    override fun newTransaction(conn: Connection?): Transaction =
        throw UnsupportedOperationException("不支持使用Connection去创建事务")

    /**
     * 使用数据源去创建事务, 可以从Spring的事务同步管理器当中去获取到Connection
     *
     * @param dataSource dataSource
     */
    override fun newTransaction(
        dataSource: DataSource,
        level: TransactionIsolationLevel?,
        autoCommit: Boolean
    ): Transaction {
        return SpringManagedTransaction(dataSource)
    }
}