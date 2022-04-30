package com.wanna.framework.context.exception

/**
 * 当前Bean正在创建中异常
 */
class BeanCurrentlyInCreationException(message: String?, cause: Throwable?) :
    BeansException(message, cause) {
    // 提供几个构造器的重载
    constructor() : this(null, null)
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}