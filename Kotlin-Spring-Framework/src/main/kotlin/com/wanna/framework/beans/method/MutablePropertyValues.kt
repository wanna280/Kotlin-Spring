package com.wanna.framework.beans.method


/**
 * 维护了PropertyValue列表
 */
class MutablePropertyValues() : PropertyValues {

    // 属性值列表
    private val propertyValueList: MutableList<PropertyValue> = ArrayList()

    constructor(mutablePropertyValues: MutablePropertyValues) : this() {
        propertyValueList.forEach(this::addPropertyValue)
    }

    override fun getPropertyValues(): Array<PropertyValue> {
        return propertyValueList.toTypedArray()
    }

    override fun containsProperty(name: String): Boolean {
        for (propertyValue in propertyValueList) {
            if (propertyValue.name == name) {
                return true
            }
        }
        return false
    }

    fun addPropertyValue(propertyValue: PropertyValue) {
        propertyValueList += propertyValue
    }

    fun addPropertyValue(name: String, value: Any?) {
        propertyValueList += PropertyValue(name, value)
    }

    fun addPropertyValues(propertyValues: Map<String, Any?>) {
        propertyValues.forEach { k, v ->
            propertyValueList.add(PropertyValue(k, v))
        }
    }

    fun addPropertyValues(propertyValues: Collection<PropertyValue>) {
        propertyValues.forEach(propertyValueList::add)
    }
}