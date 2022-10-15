package com.wanna.framework.transaction

import com.wanna.framework.core.NestedRuntimeException

/**
 * 所有事务异常的基础类
 *
 * @param msg message
 * @param cause cause
 */
abstract class TransactionException(msg: String?, cause: Throwable?) : NestedRuntimeException(msg, cause) {
    constructor(message: String?) : this(message, null)
}