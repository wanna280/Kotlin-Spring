package com.wanna.cloud.context.scope

import java.util.concurrent.ConcurrentHashMap

/**
 * 这是ScopeCache的标准实现, 基于ConcurrentHashMap去进行实现
 */
open class StandardScopeCache : ScopeCache {

    private val cache = ConcurrentHashMap<String, Any>()

    override fun get(name: String): Any? {
        return cache[name]
    }

    override fun clear(): Collection<Any> {
        val values = ArrayList(cache.values)
        cache.clear()
        return values
    }

    override fun remove(name: String): Any? {
        return this.cache.remove(name)
    }

    override fun put(name: String, value: Any): Any {
        val result = this.cache.putIfAbsent(name, value)
        return result ?: value
    }
}