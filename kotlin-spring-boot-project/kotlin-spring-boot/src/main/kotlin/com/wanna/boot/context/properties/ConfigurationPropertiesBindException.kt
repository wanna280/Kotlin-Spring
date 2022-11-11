package com.wanna.boot.context.properties

import com.wanna.framework.context.exception.BeanCreationException

/**
 * ConfigurationProperties绑定异常
 *
 * @see EnableConfigurationProperties
 * @see ConfigurationProperties
 */
open class ConfigurationPropertiesBindException(
    message: String? = null,
    private val bean: ConfigurationPropertiesBean? = null,
    cause: Throwable? = null
) : BeanCreationException(message, cause, bean?.getName())