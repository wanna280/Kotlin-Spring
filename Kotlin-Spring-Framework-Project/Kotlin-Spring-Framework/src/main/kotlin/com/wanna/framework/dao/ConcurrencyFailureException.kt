package com.wanna.framework.dao

/**
 * 并发失败，子类当中主要包含乐观锁抢锁失败、抢锁失败等类型
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class ConcurrencyFailureException(message: String?, cause: Throwable?) : TransientDataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}