package com.wanna.framework.context.exception

/**
 * 声明在创建Bean过程中发生的异常
 */
open class BeansException(override val message: String?, override val cause: Throwable?) : RuntimeException() {
    // 提供几个构造器的重载
    constructor() : this(null, null)
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}