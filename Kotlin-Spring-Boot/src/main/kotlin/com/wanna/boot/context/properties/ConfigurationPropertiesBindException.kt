package com.wanna.boot.context.properties

import com.wanna.framework.context.exception.BeanCreationException

/**
 * ConfigurationProperties绑定异常
 *
 * @see EnableConfigurationProperties
 * @see ConfigurationProperties
 */
open class ConfigurationPropertiesBindException(
    private val bean: ConfigurationPropertiesBean?,
    cause: Throwable
) : BeanCreationException(null, cause, null) {

}