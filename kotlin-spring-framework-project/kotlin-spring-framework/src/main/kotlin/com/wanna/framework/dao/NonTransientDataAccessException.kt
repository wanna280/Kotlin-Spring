package com.wanna.framework.dao

/**
 * 非瞬时(NonTransient)的数据访问异常, 继承自[DataAccessException]; 
 * 出现这个异常的情况下, 如果采用相同的操作去进行重试, 结果仍旧是失败的.
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 * @see java.sql.SQLNonTransientException
 */
abstract class NonTransientDataAccessException(message: String?, cause: Throwable?) :
    DataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}