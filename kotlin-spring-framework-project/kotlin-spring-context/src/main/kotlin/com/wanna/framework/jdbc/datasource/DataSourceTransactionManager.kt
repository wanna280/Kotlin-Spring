package com.wanna.framework.jdbc.datasource

import com.wanna.framework.transaction.TransactionDefinition
import com.wanna.framework.transaction.TransactionSystemException
import com.wanna.framework.transaction.support.AbstractPlatformTransactionManager
import com.wanna.framework.transaction.support.DefaultTransactionStatus
import com.wanna.framework.transaction.support.TransactionSynchronizationManager
import com.wanna.common.logging.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource

/**
 * 基于数据源的事务同步管理器
 *
 * @see DataSource
 * @see AbstractPlatformTransactionManager
 */
open class DataSourceTransactionManager : AbstractPlatformTransactionManager() {

    companion object {
        private val logger = LoggerFactory.getLogger(DataSourceTransactionManager::class.java)
    }

    // 要使用的数据源? 
    private var dataSource: DataSource? = null

    // 是否强制只读? 
    private var enforceReadOnly = false

    /**
     * 获取一个数据源的事务, 尝试从事务同步管理器当中去进行获取(key-DataSource)连接
     * 这里有可能并不能获取到连接, 因为之前事务同步管理器当中还没存在有连接
     *
     * @return 数据源事务对象(DataSourceTransactionObject)
     */
    override fun doGetTransaction(): Any {
        val txObject = DataSourceTransactionObject()
        txObject.setSavepointAllowed(isNestedTransactionAllowed())

        // 尝试根据数据源, 从事务同步管理器当中去获取Connection(有可能为null)
        val connectionHolder = TransactionSynchronizationManager.getResource(obtainDataSource()) as ConnectionHolder?
        txObject.setConnectionHolder(connectionHolder, false)
        return txObject
    }

    /**
     * 开始执行一个数据源的事务, 并将相关信息存储在DataSourceTransactionObject/ConnectionHolder当中
     *
     * @param definition 事务属性信息
     * @param transaction 数据源事务对象(DataSourceTransactionObject)
     */
    override fun doBegin(definition: TransactionDefinition, transaction: Any) {
        val txObject = transaction as DataSourceTransactionObject
        var connection: Connection? = null
        try {
            // 如果之前没有从事务同步管理器当中获取到ConnectionHolder的话, 那么...需要从DataSource当中去获取Connection
            if (!txObject.hasConnectionHolder()) {
                txObject.setConnectionHolder(
                    ConnectionHolder(obtainDataSource().connection),
                    true
                )  // for new ConnectionHolder
            }

            val connectionHolder =
                txObject.getConnectionHolder() ?: throw IllegalStateException("无法获取到ConnectionHolder")
            connection = connectionHolder.connection

            // 设置Connection是否只读、设置Connection的隔离级别, 并获取到之前的Connection的隔离级别, 并去进行保存
            val previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(connection, definition)
            txObject.setPreviousIsolationLevel(previousIsolationLevel)
            txObject.setReadOnly(definition.isReadOnly())

            // 将Jdbc连接设置为不自动提交...
            if (connection.autoCommit) {
                txObject.setMustRestoreAutoCommit(true)
                if (logger.isTraceEnabled) {
                    logger.trace("连接[$connection]设置为手动提交")
                }
                connection.autoCommit = false
            }

            // 准备事务连接(如果必要的话, 强制设置连接为只读)
            prepareTransactionalConnection(connection, definition)

            // 设置当前事务是活跃的, 可以以此作为依据, 判断之前是否已经有了事务...
            connectionHolder.setTransactionActive(true)

            // 从事务属性当中决策出来合适的timeout, 设置到ConnectionHolder当中
            val timeout = determineTimeout(definition)
            if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                connectionHolder.setTimeoutInSeconds(timeout)
            }

            // 如果当前Jdbc连接它是一条新连接的话, 那么需要保存到事务同步管理器当中, 方便后续从事务同步管理器当中获取
            if (txObject.isNewConnectionHolder()) {
                TransactionSynchronizationManager.bindResource(obtainDataSource(), connectionHolder)
            }
        } catch (ex: Exception) {
            if (txObject.isNewConnectionHolder()) {
                DataSourceUtils.releaseConnection(connection, dataSource)
                txObject.setConnectionHolder(null, false)
            }
            throw IllegalStateException("无法创建事务", ex)
        }
    }

    /**
     *  * 1.如果有自定义的Timeout, 那么使用自定义的Timeout
     *  * 2.如果没有自定义的, 那么使用默认的
     *
     *  @return timeout
     */
    protected open fun determineTimeout(definition: TransactionDefinition): Int {
        if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
            return definition.getTimeout()
        }
        return getDefaultTimeout()
    }

    /**
     * 准备事务连接, 如果当前事务是强制只读的话, 那么需要执行一条SQL(SET TRANSACTION READ ONLY)去让连接变得只读
     *
     * @param connection Jdbc Connection
     * @param definition 事务的属性信息
     */
    protected open fun prepareTransactionalConnection(connection: Connection, definition: TransactionDefinition) {
        if (isEnforceReadOnly() && definition.isReadOnly()) {
            var stmt: Statement? = null
            try {
                stmt = connection.createStatement()
                stmt!!.executeUpdate("SET TRANSACTION READ ONLY")
            } finally {
                stmt?.close()
            }
        }
    }

    /**
     * 挂起当前的事务, 因为需要去执行新的事务, 需要把之前的连接去进行return
     *
     * * 1.将当前的事务对象的Connection当中的ConnectionHolder设置为null
     * * 2.将TransactionSynchronizationManager当中的连接直接移除掉, 并返回
     *
     * @param transaction 要去进行挂起的事务
     * @return 要去进行挂起的资源(Connection)
     */
    override fun doSuspend(transaction: Any): Any {
        val txObject = transaction as DataSourceTransactionObject
        txObject.setConnectionHolder(null)
        return TransactionSynchronizationManager.unbindResource(obtainDataSource())
    }


    /**
     * 恢复之前的事务, 将之前的事务当中的Connection, 重新设置到事务同步管理器当中
     *
     * @param transaction 之前的事务
     * @param suspendedResources 之前挂起的资源(也就是之前的Connection)
     */
    override fun doResume(transaction: Any?, suspendedResources: Any) {
        TransactionSynchronizationManager.bindResource(obtainDataSource(), suspendedResources)
    }

    override fun doCommit(status: DefaultTransactionStatus) {
        val txObject = status.getTransaction() as DataSourceTransactionObject
        val connection = txObject.getConnectionHolder()?.connection
            ?: throw IllegalStateException("无法从DataSourceTransactionObject获取到连接")
        try {
            connection.commit()
        } catch (ex: SQLException) {
            throw TransactionSystemException("提交事务失败", ex)
        }

    }

    override fun doRollback(status: DefaultTransactionStatus) {
        val txObject = status.getTransaction() as DataSourceTransactionObject
        val connection = txObject.getConnectionHolder()?.connection
            ?: throw IllegalStateException("无法从DataSourceTransactionObject获取到连接")
        try {
            connection.rollback()
        } catch (ex: SQLException) {
            throw TransactionSystemException("回滚事务失败", ex)
        }

    }

    /**
     * 判断是否是已经存在的事务, 前提必须transaction当中有ConnectionHolder, 并且该连接是Active的, 那么说明它是一个已经存在的事务
     *
     * @param transaction 数据源事务对象
     */
    override fun isExistingTransaction(transaction: Any): Boolean {
        return transaction is DataSourceTransactionObject && transaction.hasConnectionHolder() && transaction.getConnectionHolder()!!
            .isTransactionActive()
    }

    open fun setDataSource(dataSource: DataSource) {
        this.dataSource = dataSource
    }

    protected open fun obtainDataSource(): DataSource {
        return dataSource ?: throw IllegalStateException("DataSource不能为null")
    }

    open fun setEnforceReadOnly(enforceReadOnly: Boolean) {
        this.enforceReadOnly = enforceReadOnly
    }

    open fun isEnforceReadOnly() = this.enforceReadOnly

    class DataSourceTransactionObject {

        // ConnectionHolder
        private var connectionHolder: ConnectionHolder? = null

        // 是否是一个新的连接? 
        private var newConnectionHolder = false

        // 是否允许safePoint? 
        private var isSafePointAllow = true

        // 是否只读? 
        private var readOnly: Boolean = false

        // 先前的隔离级别, 有可能为null
        private var previousIsolationLevel: Int? = null

        // 是否必须要存储自动提交信息
        private var mustRestoreAutoCommit: Boolean = false

        fun setConnectionHolder(connectionHolder: ConnectionHolder?, newHolder: Boolean) {
            this.newConnectionHolder = newHolder
            this.connectionHolder = connectionHolder
        }

        fun setConnectionHolder(connectionHolder: ConnectionHolder?) {
            this.connectionHolder = connectionHolder
        }

        fun hasConnectionHolder() = connectionHolder != null

        fun getConnectionHolder(): ConnectionHolder? {
            return this.connectionHolder
        }

        fun setSavepointAllowed(isSafePointAllow: Boolean) {
            this.isSafePointAllow = isSafePointAllow
        }

        fun isSafePointAllow() = this.isSafePointAllow

        fun setReadOnly(readOnly: Boolean) {
            this.readOnly = readOnly
        }

        fun setMustRestoreAutoCommit(mustRestoreAutoCommit: Boolean) {
            this.mustRestoreAutoCommit = mustRestoreAutoCommit
        }

        fun isMustRestoreAutoCommit() = this.mustRestoreAutoCommit

        fun isReadOnly() = this.readOnly

        fun setPreviousIsolationLevel(previousIsolationLevel: Int?) {
            this.previousIsolationLevel = previousIsolationLevel
        }

        fun getPreviousIsolationLevel() = this.previousIsolationLevel

        /**
         * 是否是一条新的JDBC连接? 
         *
         * @return 如果是新连接, return true; 否则return false
         */
        fun isNewConnectionHolder() = newConnectionHolder
    }
}