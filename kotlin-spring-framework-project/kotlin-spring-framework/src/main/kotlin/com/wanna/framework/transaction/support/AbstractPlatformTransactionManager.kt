package com.wanna.framework.transaction.support

import com.wanna.framework.transaction.PlatformTransactionManager
import com.wanna.framework.transaction.TransactionDefinition
import com.wanna.framework.transaction.TransactionStatus
import org.slf4j.LoggerFactory

/**
 * PlatformTransactionManager的抽象实现, 为所有的平台事务管理器提供模板方法的实现
 *
 * @see PlatformTransactionManager
 * @see com.wanna.framework.jdbc.datasource.DataSourceTransactionManager
 */
abstract class AbstractPlatformTransactionManager : PlatformTransactionManager {

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractPlatformTransactionManager::class.java)
    }

    // 是否允许事务的嵌套? 默认为true
    private var nestedTransactionAllowed: Boolean = true

    // 默认的超时时间
    private var defaultTimeout: Int = TransactionDefinition.TIMEOUT_DEFAULT

    open fun getDefaultTimeout(): Int = defaultTimeout

    open fun setDefaultTimeout(timeout: Int) {
        this.defaultTimeout = timeout
    }

    open fun setNestedTransactionAllowed(nestedTransactionAllowed: Boolean) {
        this.nestedTransactionAllowed = nestedTransactionAllowed
    }

    open fun isNestedTransactionAllowed() = this.nestedTransactionAllowed


    /**
     * 获取一个事务
     *
     * @param definition 事务属性信息
     * @return 维护了事务相关信息的TransactionStatus
     */
    override fun getTransaction(definition: TransactionDefinition?): TransactionStatus {
        val def = definition ?: TransactionDefinition.withDefault()

        val debugEnabled = logger.isDebugEnabled

        // 获取一个事务对象(如果之前已经有连接了, 那么直接获取连接并设置到事务对象当中)
        val transaction = doGetTransaction()

        // 如果之前已经存在过事务了, 那么需要处理嵌套事务的情况, 就需要去考虑各种事务传播属性
        if (isExistingTransaction(transaction)) {
            return handleExistingTransaction(def, transaction, debugEnabled)
        }

        // 检查事务属性当中的超时时间是否不合法
        if (def.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
            throw IllegalStateException("事务的超时时间不合法[${def.getTimeout()}]")
        }

        // 如果当前是一个新事务, 那么需要检查当前的传播属性, 去进行事务的创建工作
        if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
            throw IllegalStateException("没有找到已经存在的事务, MANDATORY的传播属性无法使用")

        } else if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW
            || def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED
            || def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED
        ) {
            // 挂起当前事务(当时之前没有事务, 因此这里使用transaction=null去进行挂起), 并返回挂起的资源(比如Connection)
            val suspendedResources = suspend(null)
            try {
                // 开始一个事务
                return startTransaction(def, transaction, debugEnabled, suspendedResources)
            } catch (ex: Throwable) {
                // 如果ex是RuntimeException或者是Error, 那么得把挂起的资源(比如Connection)去进行恢复
                if (ex is RuntimeException || ex is java.lang.Error) {
                    resume(null, suspendedResources)
                }
                throw ex
            }
        } else {
            // TODO
            return startTransaction(def, transaction, debugEnabled, null)
        }
    }

    /**
     * 开始一个事务
     *
     * @param definition 事务属性信息
     * @param transaction 事务对象
     * @param debugEnabled 是否debugEnabled
     * @param suspendedResources 挂起的资源
     */
    private fun startTransaction(
        definition: TransactionDefinition, transaction: Any, debugEnabled: Boolean, suspendedResources: Any?
    ): TransactionStatus {
        // 创建TransactionStatus, 并将事务属性信息和事务对象等信息去进行保存到TransactionStatus当中
        val status = newTransactionStatus(definition, transaction, true, suspendedResources)

        // doBegin, 准备事务的各种信息, 开始执行一个事务
        doBegin(definition, transaction)

        // 准备一些同步信息, 设置到事务同步管理器当中
        prepareSynchronization(status, definition)
        return status
    }

    /**
     * 根据给定的这些参数, 去新创建一个TransactionStatus, 去将这些给定的参数去进行设置到TransactionStatus当中
     *
     * @param definition 事务属性信息
     * @param transaction 事务对象
     * @param newTransaction 当前是否是一个新的事务? 
     * @param suspendedResources 之前挂起事务的资源
     * @return 创建好的TransactionStatus
     */
    protected open fun newTransactionStatus(
        definition: TransactionDefinition, transaction: Any, newTransaction: Boolean, suspendedResources: Any?
    ): TransactionStatus {
        return DefaultTransactionStatus(transaction, newTransaction, definition.isReadOnly(), suspendedResources)
    }

    /**
     * 挂起当前事务, 保存之前的事务信息, 并返回挂起的资源(比如Connection)
     *
     * @param transaction 事务对象
     * @return SuspendedResourcesHolder
     */
    protected open fun suspend(transaction: Any?): SuspendedResourcesHolder? {
        if (transaction != null) {
            // 挂起当前事务, 并返回之前事务同步管理器当中的资源(比如Connection资源)
            val suspendedResources = doSuspend(transaction)

            // 将要挂起的资源去包装到SuspendedResourcesHolder当中
            return SuspendedResourcesHolder(suspendedResources)
        }
        return null
    }

    /**
     * 准备一些事务的同步信息, 将当前事务的相关信息, 维护到事务同步管理器(ThreadLocal)当中
     *
     * @param status 事务的状态信息
     * @param definition 事务属性信息
     */
    protected open fun prepareSynchronization(status: TransactionStatus, definition: TransactionDefinition) {

    }

    override fun rollback(status: TransactionStatus) {
        doRollback(status as DefaultTransactionStatus)
    }

    override fun commit(status: TransactionStatus) {
        if (status.isCompleted()) {
            throw IllegalStateException("事务已经完成了, 不能重复去进行提交")
        }
        status as DefaultTransactionStatus
        try {
            doCommit(status)
        } finally {
            cleanupAfterCompletion(status)
        }
    }

    protected open fun cleanupAfterCompletion(status: DefaultTransactionStatus) {
        status.setCompleted()  // 设置事务已经完成

        // 如果必要的话, 需要根据挂起的资源, 去进行恢复
        resume(status.getTransaction(), status.getSuspendedResources() as SuspendedResourcesHolder?)
    }

    protected fun resume(transaction: Any?, suspendedResources: SuspendedResourcesHolder?) {
        if (suspendedResources != null) {
            // 如果之前有挂起的资源, 那么需要去进行恢复
            val resources = suspendedResources.suspendedResources
            if (resources != null) {
                doResume(transaction, resources)
            }
        }
    }

    /**
     * 处理已经存在有事务的情况, 也就是嵌套事务的情况, 需要检查事务的各个传播属性, 从而使用不同的方式去执行内部嵌套事务
     *
     * @param definition 事务属性信息
     * @param transaction 事务对象
     * @param debugEnabled 是否开启了debug的日志? 
     * @return TransactionStatus
     */
    protected open fun handleExistingTransaction(
        definition: TransactionDefinition, transaction: Any, debugEnabled: Boolean
    ): TransactionStatus {
        // 如果是配置成为不需要进行事务的传播, 但是恰恰出现了事务的传播情况的话, 那么直接抛出异常
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
            throw IllegalStateException("期望的是不需要出现事务[PROPAGATION_NEVER], 但是实际上出现了事务！")
        }
        // 如果传播行为被配置成为REQUIRES_NEW, 说明需要挂起之前的事务, 并启动一个新的事务
        // 挂起事务时, 会将之前的事务所拥有的资源全部进行清空, 保证startTransaction时可以重新获取资源
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
            if (debugEnabled) {
                logger.debug("事务[${definition.getName()}]的传播行为是[PROPAGATION_REQUIRES_NEW], 挂起当前事务并创建一个新的事务")
            }
            val suspendedResourcesHolder = suspend(transaction)  // suspend Resources
            try {
                return startTransaction(definition, transaction, debugEnabled, suspendedResourcesHolder);
            } catch (ex: Exception) {
                throw ex;
            }
        }
        return DefaultTransactionStatus(transaction, false, definition.isReadOnly(), null)
    }

    /**
     * 是否之前已经存在过事务了? 
     *
     * @param transaction 事务对象
     * @return 如果之前已经存在了事务, 那么return true; 没有存在则return false
     */
    protected open fun isExistingTransaction(transaction: Any): Boolean = false

    /**
     * 获取一个事务, 如果之前已经有连接的话, 那么需要从事务同步管理器当中去获取到之前的连接
     *
     * @return 事务对象(Object), 具体类型交给具体的实现去进行指定
     */
    protected abstract fun doGetTransaction(): Any

    /**
     * 开始一个事务(具体的开始逻辑, 交给子类去进行实现)
     *
     * @param definition 事务的属性信息
     * @param transaction 事务对象
     */
    protected abstract fun doBegin(definition: TransactionDefinition, transaction: Any)

    /**
     * 挂起一个事务(具体的挂起逻辑, 交给子类去进行实现)
     *
     * @param transaction 需要去进行挂起的事务
     */
    protected open fun doSuspend(transaction: Any): Any =
        throw UnsupportedOperationException("不支持挂起一个事务")

    /**
     * 恢复一个事务(具体的恢复逻辑, 交给子类去进行实现)
     *
     * @param transaction 需要进行恢复的事务
     * @param suspendedResources 挂起的资源
     */
    protected open fun doResume(transaction: Any?, suspendedResources: Any): Unit =
        throw UnsupportedOperationException("不支持去恢复一个事务")

    /**
     * 提交一个事务(具体的提交方式, 交给子类去进行实现)
     *
     * @param status 事务信息(里面包装了事务对象)
     */
    protected abstract fun doCommit(status: DefaultTransactionStatus)

    /**
     * 回滚一个事务(具体的回滚方式, 交给子类去进行实现)
     *
     * @param status 事务信息(里面包装了事务对象)
     */
    protected abstract fun doRollback(status: DefaultTransactionStatus)

    /**
     * 被挂起的资源
     *
     * @param suspendedResources 要去进行挂起的资源(比如Connection)
     */
    protected class SuspendedResourcesHolder(val suspendedResources: Any?) {

    }
}