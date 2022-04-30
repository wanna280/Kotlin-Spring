package com.wanna.framework.beans

/**
 * 标识这是一个属性的访问器，可以支持对一个属性的设置以及获取
 *
 * @see com.wanna.framework.context.BeanWrapperImpl
 * @see com.wanna.framework.context.BeanWrapper
 */
interface PropertyAccessor {

    fun setPropertyValue(name: String, value: Any?)

    fun setPropertyValue(propertyValue: PropertyValue)

    fun getPropertyValue(name: String): Any?

    fun getPropertyType(name: String): Class<*>?

    fun setPropertyValues(pvs: PropertyValues)

    fun setPropertyValues(pvs: Map<String, Any?>)
}