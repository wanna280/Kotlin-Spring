package com.wanna.framework.context

import com.wanna.framework.context.exception.BeansException

/**
 * Exception for ApplicationContext
 */
open class ApplicationContextException(message: String?, cause: Throwable?) : BeansException(message, cause) {
    // 提供几个构造器的重载
    constructor() : this(null, null)
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}