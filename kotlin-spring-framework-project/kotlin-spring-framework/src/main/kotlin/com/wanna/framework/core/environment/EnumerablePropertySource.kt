package com.wanna.framework.core.environment

/**
 * 这是一个可以被枚举的PropertySource，主要提供getPropertyNames方法去获取所有的属性值的name列表
 *
 * @see PropertySource
 */
@Suppress("UNCHECKED_CAST")
abstract class EnumerablePropertySource<T>(name: String, source: T) : PropertySource<T>(name, source) {
    constructor(name: String) : this(name, Any() as T)

    abstract fun getPropertyNames(): Array<String>
}