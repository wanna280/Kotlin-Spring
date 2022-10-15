package com.wanna.framework.dao

/**
 * 数据库当中的出现了键重复的情况会丢出这个异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 */
open class DuplicateKeyException(message: String?, cause: Throwable?) : DataIntegrityViolationException(message, cause) {
    constructor(message: String?) : this(message, null)
}