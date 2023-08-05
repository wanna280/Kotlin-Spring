package com.wanna.framework.dao

/**
 * 在发生更新时不能去抢锁, 例如"select for update"
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class CannotAcquireLockException(message: String?, cause: Throwable?) : PessimisticLockingFailureException(message, cause) {
    constructor(message: String?) : this(message, null)
}