package com.wanna.framework.core.environment

/**
 * 这是一个属性名可以被枚举的[PropertySource]，主要提供[getPropertyNames]方法去获取所有的属性值的name列表
 *
 * @see PropertySource
 * @see MapPropertySource
 */
@Suppress("UNCHECKED_CAST")
abstract class EnumerablePropertySource<T>(name: String, source: T) : PropertySource<T>(name, source) {
    constructor(name: String) : this(name, Any() as T)

    /**
     * 获取当前PropertySource当中的所有的属性名列表
     *
     * @return list of property name
     */
    abstract fun getPropertyNames(): Array<String>
}