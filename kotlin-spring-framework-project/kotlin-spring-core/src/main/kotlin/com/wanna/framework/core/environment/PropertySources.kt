package com.wanna.framework.core.environment

/**
 * 这是一个多个PropertySources的聚合
 */
interface PropertySources : Iterable<PropertySource<*>> {

    fun contains(name: String): Boolean

    fun get(name: String): PropertySource<*>?
}