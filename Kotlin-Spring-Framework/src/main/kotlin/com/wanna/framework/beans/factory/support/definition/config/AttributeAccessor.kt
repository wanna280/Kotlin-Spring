package com.wanna.framework.beans.factory.support.definition.config

/**
 * 这是一个属性的访问器，实现这个接口的子类，可以支持属性的访问
 */
interface AttributeAccessor {

    /**
     * 根据name-value设置属性
     */
    fun setAttribute(name: String, value: Any?)

    /**
     * 根据name获取属性
     */
    fun getAttribute(name: String): Any?

    /**
     * 是否包含这个属性
     */
    fun hasAttribute(name: String): Boolean

    /**
     * 获取所有的属性name数组
     */
    fun attributeNames(): Array<String>

    /**
     * 根据name去移除一个属性
     */
    fun removeAttribute(name: String): Any?
}