package com.wanna.framework.beans

import com.wanna.framework.lang.Nullable

/**
 * 在解析嵌套路径时, 遇到了空值的异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/14
 */
open class NullValueInNestedPathException(
    beanClass: Class<*>, propertyName: String, @Nullable message: String?,
    @Nullable cause: Throwable?
) : InvalidPropertyException(beanClass, propertyName, message, cause) {

    constructor(beanClass: Class<*>, propertyName: String, message: String?) :
            this(beanClass, propertyName, message, null)

    constructor(beanClass: Class<*>, propertyName: String) :
            this(beanClass, propertyName, "Value of nested property '$propertyName' is null")

}