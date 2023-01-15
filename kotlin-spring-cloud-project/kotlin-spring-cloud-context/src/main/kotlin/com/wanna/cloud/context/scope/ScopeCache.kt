package com.wanna.cloud.context.scope

/**
 * 这是定义了一个Scope的缓存, 提供从缓存当中获取Bean, 以及将Bean注册到缓存当中
 */
interface ScopeCache {
    fun get(name: String): Any?
    fun clear(): Collection<Any>
    fun remove(name: String): Any?

    /**
     * 放入元素到缓存当中, 如果不存在才放入, 如果存在了, 那么就不放入
     *
     * @return 如果之前已经放入过了, 返回之前的; 如果之前没放入过, return value
     */
    fun put(name: String, value: Any): Any
}