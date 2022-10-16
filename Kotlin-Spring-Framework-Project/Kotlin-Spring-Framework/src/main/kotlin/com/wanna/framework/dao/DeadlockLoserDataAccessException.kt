package com.wanna.framework.dao

/**
 * 当前进程是发生死锁时的失败方(loser)，并且事务回滚失败
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class DeadlockLoserDataAccessException(message: String?, cause: Throwable?) :
    PessimisticLockingFailureException(message, cause)