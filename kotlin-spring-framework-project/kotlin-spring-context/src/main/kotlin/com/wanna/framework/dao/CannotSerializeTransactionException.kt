package com.wanna.framework.dao

/**
 * 因为更新冲突导致的事务序列化失败的情况下抛出这个异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class CannotSerializeTransactionException(message: String?, cause: Throwable?) :
    PessimisticLockingFailureException(message, cause) {
    constructor(message: String?) : this(message, null)
}