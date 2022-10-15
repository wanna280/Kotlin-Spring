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
open class SpringManagedTransaction(private val dataSource: DataSource) : Transaction {
    companion object {
        /**
         * 默认的超时时间，单位为秒
         */
        const val DEFAULT_TIME_OUT_SECONDS = 5
    }


    /**
     * 需要使用的JDBC连接
     */
    private var connection: Connection? = null

    /**
     * 是否自动提交？
     */
    private var autoCommit = true

    /**
     * 判断当前连接是否是一个事务连接？如果TransactionSynchronizationManager当中就是当前连接的话，那么为true
     */
    private var isTransactionConnection = false

    /**
     * 获取JDBC的Connection，如果之前没有初始化过的话，在这里去完成初始化工作
     *
     * @return JDBC连接
     */
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

    /**
     * 提交，只有当前连接不是一个Spring事务连接的情况下，才允许提交
     */
    override fun commit() {
        if (!isTransactionConnection) {
            connection?.commit()
        }
    }

    /**
     * 回滚，只有当前连接不是一个Spring事务连接的情况下，才允许回滚
     */
    override fun rollback() {
        if (!isTransactionConnection) {
            connection?.rollback()
        }
    }

    override fun close() {
        DataSourceUtils.releaseConnection(connection, dataSource)
    }

    /**
     * 获取当前事务的超时时间(单位为秒)
     *
     * @return 连接超时时间，默认为5s
     */
    override fun getTimeout(): Int {
        val connectionHolder = TransactionSynchronizationManager.getResource(dataSource) as ConnectionHolder
        val timeout = connectionHolder.getTimeInSeconds().toInt()
        return if (timeout == -1) DEFAULT_TIME_OUT_SECONDS else timeout
    }
}