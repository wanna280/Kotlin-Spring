package com.wanna.framework.dao

/**
 * 权限不允许的异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class PermissionDeniedDataAccessException(message: String?, cause: Throwable?) :
    NonTransientDataAccessException(message, cause)