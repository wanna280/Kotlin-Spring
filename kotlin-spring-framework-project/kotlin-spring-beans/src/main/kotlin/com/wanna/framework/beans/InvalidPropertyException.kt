package com.wanna.framework.beans

import com.wanna.framework.lang.Nullable

/**
 * 不合法的属性异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/14
 */
open class InvalidPropertyException(
    val beanClass: Class<*>,
    val propertyName: String,
    @Nullable message: String?,
    @Nullable cause: Throwable?
) : FatalBeanException(message, cause) {
    constructor(beanClass: Class<*>, propertyName: String, message: String?) :
            this(beanClass, propertyName, message, null)
}