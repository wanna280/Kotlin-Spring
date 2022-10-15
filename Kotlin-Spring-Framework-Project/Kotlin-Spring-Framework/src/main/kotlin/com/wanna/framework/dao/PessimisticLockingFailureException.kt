package com.wanna.framework.dao

/**
 * 悲观锁抢锁("PessimisticLock")失败的异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 * @see DeadlockLoserDataAccessException
 * @see CannotAcquireLockException
 * @see CannotSerializeTransactionException
 */
open class PessimisticLockingFailureException(message: String?, cause: Throwable?) :
    TransientDataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}