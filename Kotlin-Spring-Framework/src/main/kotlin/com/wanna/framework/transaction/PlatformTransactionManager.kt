package com.wanna.framework.transaction

/**
 * 平台事务的管理器，支持去获取事务、提交事务、回滚事务等事务的相关操作
 *
 * @see TransactionManager
 */
interface PlatformTransactionManager : TransactionManager {

    /**
     * 根据事务属性信息，去获取一个事务
     *
     * @param definition 事务的属性信息
     * @return 事务的状态信息
     */
    fun getTransaction(definition: TransactionDefinition?): TransactionStatus

    /**
     * 回滚事务
     *
     * @param status 事务的状态信息
     */
    fun rollback(status: TransactionStatus)

    /**
     * 提交事务，如果必要的话，需要去恢复之前已经有的事务的连接
     *
     * @param status 事务的状态信息
     */
    fun commit(status: TransactionStatus)
}