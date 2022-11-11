package com.wanna.framework.beans

import com.wanna.framework.util.ClassUtils
import java.beans.PropertyChangeEvent

/**
 * 它是属性访问异常的其中一种特殊情况，属于是类型转换不匹配的情况；
 * 当从一种类型转换成为另外一种类型失败时，就会出现这个异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/20
 *
 * @param propertyChangeEvent 属性改变事件(oldValue/newValue/propertyName)
 * @param message errorMessage
 * @param cause cause
 * @param value 待完成类型转换的对象
 * @param requiredType 需要的类型
 */
open class TypeMismatchException
private constructor(
    propertyChangeEvent: PropertyChangeEvent?,
    message: String?,
    cause: Throwable?,
    val value: Any?,
    val requiredType: Class<*>
) : PropertyAccessException(propertyChangeEvent, message, cause) {
    constructor(value: Any, requiredType: Class<*>, cause: Throwable?) : this(null, null, cause, value, requiredType)

    constructor(value: Any, requiredType: Class<*>) : this(
        null, "类型不匹配！给定的对象类型为[${ClassUtils.getQualifiedName(value.javaClass)}]," +
                " 需要的对象类型为[${ClassUtils.getQualifiedName(requiredType)}]",
        null, value, requiredType
    )

    constructor(propertyChangeEvent: PropertyChangeEvent, requiredType: Class<*>, cause: Throwable?) : this(
        propertyChangeEvent,
        "类型不匹配！给定的对象类型为[${ClassUtils.getQualifiedName(propertyChangeEvent.newValue.javaClass)}], 需要的对象类型为[${
            ClassUtils.getQualifiedName(requiredType)
        }], 属性名为[${propertyChangeEvent.propertyName}]",
        cause, null, requiredType
    )

    constructor(propertyChangeEvent: PropertyChangeEvent, requiredType: Class<*>) :
            this(propertyChangeEvent, requiredType, null)
}