package com.wanna.framework.context

import com.wanna.framework.beans.BeansException

/**
 * Exception for ApplicationContext
 */
open class ApplicationContextException(message: String?, cause: Throwable?) : BeansException(message, cause) {
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}