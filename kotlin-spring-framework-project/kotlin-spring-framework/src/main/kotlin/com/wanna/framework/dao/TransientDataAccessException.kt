package com.wanna.framework.dao

/**
 * 瞬时的数据访问异常, 多次尝试可能会得到不同的结果, 比如超时异常, 重试一下可能就好了
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 * @see java.sql.SQLTransientException
 */
abstract class TransientDataAccessException(message: String?, cause: Throwable?) : DataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}