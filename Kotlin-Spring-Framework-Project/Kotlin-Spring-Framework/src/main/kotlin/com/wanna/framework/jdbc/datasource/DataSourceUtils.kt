package com.wanna.framework.jdbc.datasource

import com.wanna.framework.transaction.TransactionDefinition
import com.wanna.framework.transaction.support.TransactionSynchronizationManager
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource


/**
 * 为SpringTransaction提供DataSource操作的工具类
 *
 * @see TransactionSynchronizationManager
 */
object DataSourceUtils {

    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(DataSourceUtils::class.java)

    /**
     * 根据数据源，从事务同步管理器[TransactionSynchronizationManager]当中去去获取连接
     *
     * @param dataSource DataSource
     * @return 获取到的JDBC连接
     */
    @JvmStatic
    fun getConnection(dataSource: DataSource?): Connection {
        if (dataSource == null) {
            throw IllegalStateException("没有给定DataSource，无法获取到Connection")
        }
        return doGetConnection(dataSource)
    }

    @JvmStatic
    private fun doGetConnection(dataSource: DataSource): Connection {

        // 从TransactionSynchronizationManager当中去根据DataSource去获取Connection
        val connectionHolder = TransactionSynchronizationManager.getResource(dataSource) as ConnectionHolder?
        var connection = connectionHolder?.connection

        // 如果之前已经存在有该DataSource去缓存的连接，那么直接返回
        if (connection != null) {
            return connection
        }

        // 如果之前没有该DataSource去缓存的连接，那么需要从DataSource当中去获取到一个Connection
        connection = dataSource.connection

        // 将该Connection去绑定给当前线程(设置到ThreadLocal当中)
        TransactionSynchronizationManager.bindResource(dataSource, ConnectionHolder(connection))
        return connection
    }

    /**
     * 释放一条JDBC连接
     *
     * @param connection 连接
     * @param dataSource 数据源
     */
    @JvmStatic
    fun releaseConnection(connection: Connection?, dataSource: DataSource?) {
        try {
            doReleaseConnection(connection, dataSource)
        } catch (ex: SQLException) {
            if (logger.isDebugEnabled) {
                logger.debug("关闭一条连接失败", ex)
            }
        } catch (ex: Throwable) {
            if (logger.isDebugEnabled) {
                logger.debug("关闭连接过程中出现了未知错误", ex)
            }
        }
    }

    /**
     * 判断当前连接事务是事务连接
     *
     * @param connection JDBC连接
     * @param dataSource DataSource
     * @return 如果当前TransactionSynchronizationManager当中就是给定的Connection，那么return true；否则return false
     */
    @JvmStatic
    fun isConnectionTransactional(connection: Connection, dataSource: DataSource?): Boolean {
        dataSource ?: return false
        val connectionHolder = TransactionSynchronizationManager.getResource(connection) as ConnectionHolder?
        return connectionHolder?.connection == connection
    }

    /**
     * 如果必要的话，释放一条JDBC连接
     *
     * @param connection 连接
     * @param dataSource 数据源
     */
    @JvmStatic
    fun doReleaseConnection(connection: Connection?, dataSource: DataSource?) {
        connection ?: return
        if (dataSource != null) {
            // 从事务同步管理器当中去获取到Connection，去进行释放连接？
            val connectionHolder = TransactionSynchronizationManager.getResource(dataSource) as ConnectionHolder?
            if (connectionHolder != null && connectionHolder.connection == connection) {
                connectionHolder.released()
                return
            }
        }

        // 如果该数据源允许关闭一条连接的话，那么尝试去关闭一个连接
        doCloseConnection(connection, dataSource)
    }

    /**
     * 如果该数据源允许关闭一条连接的话，那么尝试去关闭一个连接
     *
     * @param connection 连接
     * @param dataSource 数据源
     */
    @JvmStatic
    fun doCloseConnection(connection: Connection, dataSource: DataSource?) {
        if (dataSource !is SmartDataSource || (dataSource.shouldClose(connection))) {
            connection.close()
        }
    }

    /**
     * 准备连接，设置是否只读/设置事务的隔离级别
     *
     * @param connection 连接
     * @param definition 事务的定义信息
     *
     * @return 之前的连接的事务隔离级别(有可能为null)
     */
    @JvmStatic
    fun prepareConnectionForTransaction(connection: Connection, definition: TransactionDefinition?): Int? {
        if (definition != null && definition.isReadOnly()) {
            connection.isReadOnly = true
        }

        var previousIsolationLevel: Int? = null
        if (definition != null && definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
            val currentIsolation = connection.transactionIsolation
            if (currentIsolation != definition.getIsolationLevel()) {
                previousIsolationLevel = currentIsolation
                connection.transactionIsolation = definition.getIsolationLevel()
            }
        }

        return previousIsolationLevel
    }
}