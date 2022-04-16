package com.wanna.framework.context.exception

/**
 * 要进行注入的元素和需要的类型不匹配异常
 */
class BeanNotOfRequiredTypeException(override val message: String?, override val cause: Throwable?) :
    BeansException(message, cause) {
    // 提供几个构造器的重载
    constructor() : this(null, null)
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}