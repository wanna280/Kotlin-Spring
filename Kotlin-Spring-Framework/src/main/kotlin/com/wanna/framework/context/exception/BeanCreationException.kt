package com.wanna.framework.context.exception

class BeanCreationException(override val message: String?, override val cause: Throwable?) :
    BeansException() {
    // 提供几个构造器的重载
    constructor() : this(null, null)
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}