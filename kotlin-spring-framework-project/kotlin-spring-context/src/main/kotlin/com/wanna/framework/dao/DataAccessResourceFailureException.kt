package com.wanna.framework.dao

/**
 * 访问资源失败的异常, 比如我们无法通过JDBC去连接到数据库
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class DataAccessResourceFailureException(message: String?, cause: Throwable?) :
    NonTransientDataAccessResourceException(message, cause) {
    constructor(message: String?) : this(message, null)
}