package com.wanna.framework.dao

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class InvalidDataAccessResourceUsageException(message: String?, cause: Throwable?) :
    NonTransientDataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}