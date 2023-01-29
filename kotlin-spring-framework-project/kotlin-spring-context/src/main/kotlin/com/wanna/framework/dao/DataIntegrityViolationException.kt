package com.wanna.framework.dao

/**
 * 违反数据库的约束条件异常, 例如主键重复的[DuplicateKeyException]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 */
open class DataIntegrityViolationException(message: String?, cause: Throwable?) :
    NonTransientDataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}