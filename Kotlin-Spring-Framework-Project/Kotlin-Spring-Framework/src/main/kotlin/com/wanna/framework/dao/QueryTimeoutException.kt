package com.wanna.framework.dao

/**
 * 查询超时异常，它是一个瞬时的数据访问异常[TransientDataAccessException]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class QueryTimeoutException(message: String?, cause: Throwable?) : TransientDataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}