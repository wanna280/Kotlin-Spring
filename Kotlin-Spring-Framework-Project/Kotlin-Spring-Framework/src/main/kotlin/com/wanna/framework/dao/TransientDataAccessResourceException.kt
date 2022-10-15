package com.wanna.framework.dao

/**
 * 持久的数据访问资源异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class TransientDataAccessResourceException(message: String?, cause: Throwable?) :
    TransientDataAccessException(message, cause) {
    constructor(message: String?) : this(message, null)
}