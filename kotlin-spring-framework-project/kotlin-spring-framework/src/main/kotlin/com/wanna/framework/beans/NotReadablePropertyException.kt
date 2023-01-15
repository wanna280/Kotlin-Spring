package com.wanna.framework.beans

import com.wanna.framework.lang.Nullable

/**
 * 给定的Bean属性不可读(可能不包含Getter)
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/15
 */
open class NotReadablePropertyException(
    beanClass: Class<*>,
    propertyName: String,
    @Nullable message: String?,
    @Nullable cause: Throwable?
) : InvalidPropertyException(beanClass, propertyName, message, cause) {

    constructor(beanClass: Class<*>, propertyName: String, message: String?) :
            this(beanClass, propertyName, message, null)

    constructor(beanClass: Class<*>, propertyName: String) :
            this(
                beanClass,
                propertyName,
                "Bean property '$propertyName' is not readable or has an invalid getter method: Does the return type of the getter match the parameter type of the setter?"
            )
}