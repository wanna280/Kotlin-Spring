package com.wanna.framework.context.exception

open class BeanCreationException(message: String?, cause: Throwable?) :
    BeansException(message, cause) {
    // 提供几个构造器的重载
    constructor() : this(null, null)
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}