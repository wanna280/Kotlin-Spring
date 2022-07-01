package com.wanna.framework.transaction.interceptor

/**
 * 不进行回滚的规则
 *
 * @param exceptionName 不进行回滚的异常
 */
open class NoRollbackRuleAttribute(exceptionName: String) : RollbackRuleAttribute(exceptionName) {
    constructor(exClass: Class<*>) : this(exClass.name)
}