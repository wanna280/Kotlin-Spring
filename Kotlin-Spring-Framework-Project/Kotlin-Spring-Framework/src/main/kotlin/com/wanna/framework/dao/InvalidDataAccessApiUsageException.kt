package com.wanna.framework.dao

/**
 * 不合法的数据访问的API使用方式异常，它代表的是一个Java的数据访问框架问题，
 * 而不是出现了底层的数据访问基础设施(比如DB)的问题
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class InvalidDataAccessApiUsageException(message: String?, cause: Throwable?) :
    NonTransientDataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}