package com.wanna.framework.beans.factory

import com.wanna.framework.beans.BeansException

/**
 * 当前Bean正在创建中异常
 *
 * @param message errorMessage
 * @param cause errorCause
 * @param beanName beanName
 */
open class BeanCurrentlyInCreationException(message: String?, cause: Throwable?, val beanName: String?) :
    BeansException(message, cause) {
    constructor(message: String?, cause: Throwable?) : this(message, cause, null)
    constructor(message: String?) : this(message, null, null)
}