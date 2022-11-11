package com.wanna.framework.beans

import com.wanna.framework.core.NestedRuntimeException

/**
 * 声明在创建Bean过程中发生的异常
 *
 * @param message errorMessage
 * @param cause errorCause
 */
open class BeansException(message: String?, cause: Throwable?) : NestedRuntimeException(message, cause) {
    constructor(message: String?) : this(message = message, cause = null)
}