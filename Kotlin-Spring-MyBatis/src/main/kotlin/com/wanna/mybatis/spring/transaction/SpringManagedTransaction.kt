package com.wanna.mybatis.spring.transaction

import com.wanna.framework.jdbc.datasource.ConnectionHolder
import com.wanna.framework.jdbc.datasource.DataSourceUtils
import com.wanna.framework.transaction.support.TransactionSynchronizationManager
import org.apache.ibatis.transaction.Transaction
import java.sql.Connection
import javax.sql.DataSource

/**
 * 受Spring管理的MyBatis事务，支持直接从Spring的事务同步管理器当中去获取Connection
 *
 * @param dataSource dataSource
 */
class SpringManagedTransaction(private val dataSource: DataSource) : Transaction {

    private var connection: Connection? = null

    private var autoCommit = true

    private var isTransactionConnection: Boolean = false

    override fun getConnection(): Connection {
        if (this.connection == null) {
            openConnection()
        }
        return this.connection!!
    }

    /**
     * SqlSession可以自动从Spring的事务同步管理器当中去获取到一条连接
     */
    private fun openConnection() {
        val connection = DataSourceUtils.getConnection(dataSource)
        this.connection = connection
        this.autoCommit = connection.autoCommit
        this.isTransactionConnection = DataSourceUtils.isConnectionTransactional(connection, dataSource)
    }

    override fun commit() {
        if (!isTransactionConnection) {
            connection?.commit()
        }
    }

    override fun rollback() {
        if (!isTransactionConnection) {
            connection?.rollback()
        }
    }

    override fun close() {
        DataSourceUtils.releaseConnection(connection, dataSource)
    }

    override fun getTimeout(): Int {
        val connectionHolder = TransactionSynchronizationManager.getResource(dataSource) as ConnectionHolder
        val timeout = connectionHolder.getTimeInSeconds().toInt()
        return if (timeout == -1) 5 else timeout
    }
}