package com.wanna.framework.dao

/**
 * 暂时没有连接时发生的异常, 并且这个异常是暂时的
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 * @see java.sql.SQLNonTransientConnectionException
 */
open class NonTransientDataAccessResourceException(message: String?, cause: Throwable?) :
    NonTransientDataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}