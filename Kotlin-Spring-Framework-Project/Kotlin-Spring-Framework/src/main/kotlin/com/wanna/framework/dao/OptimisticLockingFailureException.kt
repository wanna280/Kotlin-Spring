package com.wanna.framework.dao

/**
 * 乐观锁抢锁失败
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class OptimisticLockingFailureException(message: String?, cause: Throwable?) :
    TransientDataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}