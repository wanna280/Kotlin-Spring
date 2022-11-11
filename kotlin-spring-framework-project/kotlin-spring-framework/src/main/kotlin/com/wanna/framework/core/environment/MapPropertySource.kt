package com.wanna.framework.core.environment

/**
 * 这是一个Map的PropertySource，使用Map的方式去实现一个PropertySource
 *
 * @see PropertySource
 */
open class MapPropertySource(name: String, source: Map<String, Any>) :
    EnumerablePropertySource<Map<String, Any>>(name, source) {

    override fun getProperty(name: String): Any? {
        return source[name]
    }

    override fun containsProperty(name: String): Boolean {
        return source.containsKey(name)
    }

    override fun getPropertyNames(): Array<String> {
        return source.keys.toTypedArray()
    }
}