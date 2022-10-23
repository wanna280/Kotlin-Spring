package com.wanna.framework.beans

import java.util.concurrent.CopyOnWriteArrayList


/**
 * 维护了PropertyValue列表，它是PropertyValues的具体实现
 *
 * @see PropertyValue
 * @see PropertyValues
 */
open class MutablePropertyValues() : PropertyValues {

    /**
     * 内部维护的属性值列表，采用COW的方式去进行维护
     */
    private val propertyValueList: MutableList<PropertyValue> = CopyOnWriteArrayList()

    /**
     * 提供一个PropertyValues的构造器，将它里面的属性全部拷贝到当前对象的PropertyValues当中
     *
     * @param propertyValues PropertyValues
     */
    constructor(propertyValues: PropertyValues?) : this() {
        propertyValues?.getPropertyValues()?.forEach(::addPropertyValue)
    }

    constructor(propertyValues: Map<String, Any?>) : this() {
        propertyValues.forEach(::addPropertyValue)
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

    override fun hasProperty(): Boolean {
        return propertyValueList.isNotEmpty()
    }

    override fun addPropertyValue(propertyValue: PropertyValue) {
        propertyValueList += propertyValue
    }

    override fun addPropertyValue(name: String, value: Any?) {
        propertyValueList += PropertyValue(name, value)
    }

    override fun addPropertyValues(propertyValues: Map<String, Any?>) {
        propertyValues.forEach { (k, v) -> propertyValueList.add(PropertyValue(k, v)) }
    }

    override fun addPropertyValues(propertyValues: Collection<PropertyValue>) {
        propertyValues.forEach(propertyValueList::add)
    }

    override fun removePropertyValue(name: String): Boolean {
        val iterator = propertyValueList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().name == name) {
                iterator.remove()
                return true
            }
        }
        return false
    }
}