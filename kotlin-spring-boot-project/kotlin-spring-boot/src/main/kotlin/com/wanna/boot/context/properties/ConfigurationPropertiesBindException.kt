package com.wanna.boot.context.properties

import com.wanna.framework.beans.factory.exception.BeanCreationException

/**
 * 在对于一个[ConfigurationPropertiesBean]去执行属性的绑定过程当中遇到了绑定异常
 *
 * @param cause cause
 * @param message error message
 * @param bean ConfigurationPropertiesBean
 *
 * @see EnableConfigurationProperties
 * @see ConfigurationProperties
 */
open class ConfigurationPropertiesBindException(
    message: String? = null,
    private val bean: ConfigurationPropertiesBean? = null,
    cause: Throwable? = null
) : BeanCreationException(message, cause, bean?.getName())