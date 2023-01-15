package com.wanna.framework.beans

/**
 * 这是一个属性值列表, 提供对于属性值的维护, 包括添加/删除/查询等; 是Spring当中对于一个Bean当中字段的key-value去进行的一层抽象;
 * 一个字段名(key)和字段的值(value)组成了一个PropertyValue; 一个对象当中的多个PropertyValue便组成了一个PropertyValues;
 * 它的具体实现类为MutablePropertyValues
 *
 * @see PropertyValue
 * @see MutablePropertyValues
 */
interface PropertyValues {

    /**
     * 获取全部的PropertyValue列表
     *
     * @return PropertyValue列表
     */
    fun getPropertyValues(): Array<PropertyValue>

    /**
     * 遍历所有的PropertyValue去判断是否包含这个属性值？
     *
     * @param name propertyName
     * @return 当前PropertyValue列表当中是否已经包含了给定的propertyName的属性值
     */
    fun containsProperty(name: String): Boolean

    /**
     * 当前属性值列表当中是否为空
     *
     * @return 如果为空, return true; 否则, return false
     */
    fun hasProperty(): Boolean

    /**
     * 添加PropertyValue
     */
    fun addPropertyValue(propertyValue: PropertyValue)
    fun addPropertyValue(name: String, value: Any?)
    fun addPropertyValues(propertyValues: Map<String, Any?>)
    fun addPropertyValues(propertyValues: Collection<PropertyValue>)

    /**
     * 移除属性值, 如果移除成功return true; 否则return false
     *
     * @param name propertyName
     */
    fun removePropertyValue(name: String): Boolean
}