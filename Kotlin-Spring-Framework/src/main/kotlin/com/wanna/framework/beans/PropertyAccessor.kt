package com.wanna.framework.beans

import com.wanna.framework.beans.method.PropertyValue
import com.wanna.framework.beans.method.PropertyValues

/**
 * 标识这是一个属性的访问器，可以支持对一个属性的设置
 */
interface PropertyAccessor {

    fun setPropertyValue(name: String, value: Any?)

    fun setPropertyValue(propertyValue: PropertyValue)

    fun getPropertyValue(name: String): Any?

    fun getPropertyType(name: String): Class<*>?

    fun setPropertyValues(pvs: PropertyValues)

    fun setPropertyValues(pvs: Map<String, Any?>)
}