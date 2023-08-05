package com.wanna.framework.core.environment

import com.wanna.framework.lang.Nullable

/**
 * 这是一个基于Map去进行实现的[PropertySource], 使用Map作为source的方式去实现一个[PropertySource]
 *
 * @param name name
 * @param source source
 *
 * @see PropertySource
 * @see EnumerablePropertySource
 */
open class MapPropertySource(name: String, source: Map<String, Any>) :
    EnumerablePropertySource<Map<String, Any>>(name, source) {

    @Nullable
    override fun getProperty(name: String): Any? = source[name]

    override fun containsProperty(name: String): Boolean = source.containsKey(name)

    override fun getPropertyNames(): Array<String> = source.keys.toTypedArray()
}