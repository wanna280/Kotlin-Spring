package com.wanna.framework.jdbc.datasource

import com.wanna.framework.transaction.TransactionDefinition
import com.wanna.framework.transaction.support.TransactionSynchronizationManager
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource


/**
 * 为SpringTransaction提供DataSource操作的工具类
 */
object DataSourceUtils {

    private val logger = LoggerFactory.getLogger(DataSourceUtils::class.java)

    /**
     * 释放一条连接
     *
     * @param connection 连接
     * @param dataSource 数据源
     */
    @JvmStatic
    fun releaseConnection(connection: Connection?, dataSource: DataSource?) {
        try {
            releaseConnection(connection, dataSource)
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
     * 释放一条Jdbc连接
     *
     * @param connection 连接
     * @param dataSource 数据源
     */
    @JvmStatic
    fun doReleaseConnection(connection: Connection?, dataSource: DataSource?) {
        if (connection == null) {
            return
        }
        if (dataSource != null) {
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