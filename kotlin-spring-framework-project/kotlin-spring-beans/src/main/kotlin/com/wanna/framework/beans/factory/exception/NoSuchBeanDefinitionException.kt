package com.wanna.framework.beans.factory.exception

import com.wanna.framework.beans.BeansException

/**
 * 当前Bean的定义信息未在BeanFactory找到的异常
 *
 * @param message errorMessage
 * @param cause errorCause
 * @param beanName beanName
 * @param beanType beanType
 */
open class NoSuchBeanDefinitionException(
    message: String?,
    cause: Throwable?,
    val beanName: String?,
    val beanType: Class<*>?
) : BeansException(message, cause) {
    constructor(message: String?, cause: Throwable?) : this(message, cause, null, null)
    constructor(message: String?) : this(message, null, null, null)
}