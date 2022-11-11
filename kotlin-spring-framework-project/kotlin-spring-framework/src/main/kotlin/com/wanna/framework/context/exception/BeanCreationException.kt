package com.wanna.framework.context.exception

import com.wanna.framework.beans.BeansException

/**
 * Bean创建过程中发生的异常
 *
 * @param beanName 创建出现问题的beanName
 * @param message errorMessage
 * @param cause errorCause
 */
open class BeanCreationException(message: String?, cause: Throwable?, val beanName: String?) :
    BeansException(message, cause) {
    constructor(message: String?) : this(message, null, null)
    constructor(message: String?, cause: Throwable?) : this(message, cause, null)
}