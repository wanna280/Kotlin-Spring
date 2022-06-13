package com.wanna.framework.transaction.support

import com.wanna.framework.transaction.TransactionDefinition
import com.wanna.framework.transaction.TransactionDefinition.Companion.ISOLATION_DEFAULT
import com.wanna.framework.transaction.TransactionDefinition.Companion.PROPAGATION_REQUIRED
import com.wanna.framework.transaction.TransactionDefinition.Companion.TIMEOUT_DEFAULT

/**
 * 对于TransactionDefinition的具体实现，对需要提供的各个属性提供了getter/setter；
 * 各个getter/setter方法都会被直接设置为final的，不允许子类去进行重写
 *
 * @see TransactionDefinition
 */
open class DefaultTransactionDefinition : TransactionDefinition {
    companion object {
        const val PREFIX_PROPAGATION = "PROPAGATION_"
        const val PREFIX_ISOLATION = "ISOLATION_"
        const val PREFIX_TIMEOUT = "timeout_"
        const val READ_ONLY_MARKER = "readOnly"
    }

    // 事务的传播行为
    private var propagationBehavior: Int = PROPAGATION_REQUIRED

    // 事务的隔离级别
    private var isolationLevel: Int = ISOLATION_DEFAULT

    // 事务的超时时间
    private var timeout: Int = TIMEOUT_DEFAULT

    // 事务是否是只读的事务？
    private var readOnly = false

    // 事务的name，默认为null
    private var name: String? = null

    final override fun getPropagationBehavior() = propagationBehavior

    fun setPropagationBehavior(propagationBehavior: Int) {
        this.propagationBehavior = propagationBehavior
    }

    final override fun getIsolationLevel() = isolationLevel

    fun setIsolationLevel(isolationLevel: Int) {
        this.isolationLevel = isolationLevel
    }

    final override fun getTimeout() = timeout

    fun setTimeout(timeout: Int) {
        this.timeout = timeout
    }

    final override fun isReadOnly() = readOnly

    fun setReadOnly(readOnly: Boolean) {
        this.readOnly = readOnly
    }

    final override fun getName() = name

    fun setName(name: String) {
        this.name = name
    }
}