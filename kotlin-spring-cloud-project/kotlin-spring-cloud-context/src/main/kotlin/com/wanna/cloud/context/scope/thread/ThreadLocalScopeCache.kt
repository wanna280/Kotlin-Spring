package com.wanna.cloud.context.scope.thread

import com.wanna.cloud.context.scope.ScopeCache

/**
 * ThreadLocal的ScopeCache;
 * Why: Spring源码当中, 为什么用的ConcurrentHashMap? ? ? 又没有线程竞争的情况, 明明HashMap已经足够
 */
open class ThreadLocalScopeCache : ScopeCache {

    private val cache = object : ThreadLocal<HashMap<String, Any>>() {
        override fun initialValue(): HashMap<String, Any> {
            return HashMap()
        }
    }

    override fun get(name: String): Any? {
        return this.cache.get()[name]
    }

    override fun clear(): Collection<Any> {
        val data = this.cache.get()
        val values = ArrayList(data.values)
        data.clear()
        return values
    }

    override fun remove(name: String): Any? {
        return this.cache.get().remove(name)
    }

    override fun put(name: String, value: Any): Any {
        val result = this.cache.get().putIfAbsent(name, value)
        if (result != null) {
            return result
        }
        return value
    }
}