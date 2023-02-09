package com.wanna.framework.util

/**
 * 多值Map的适配器, 为所有的子类去提供模板方法实现
 *
 * @param targetMap 承载数据的最终Map
 *
 * @param K keyType
 * @param V valueType
 */
open class MultiValueMapAdapter<K, V>(private val targetMap: MutableMap<K, MutableList<V>>) : MultiValueMap<K, V> {

    override fun getFirst(key: K): V? {
        val values = targetMap[key] ?: return null
        return if (values.isEmpty()) null else values.iterator().next()
    }

    override fun add(key: K, value: V) {
        targetMap.putIfAbsent(key, ArrayList())
        targetMap[key]!! += value
    }

    override fun addAll(key: K, values: List<V>) {
        targetMap.putIfAbsent(key, ArrayList())
        targetMap[key]!! += values
    }

    override fun set(key: K, value: V) {
        val values = ArrayList(listOf(value))
        targetMap[key] = values
    }

    override val size: Int
        get() = targetMap.size

    override fun containsKey(key: K): Boolean {
        return targetMap.containsKey(key)
    }

    override fun containsValue(value: MutableList<V>): Boolean {
        return targetMap.containsValue(value)
    }

    override fun get(key: K): MutableList<V>? {
        return targetMap[key]
    }

    override fun isEmpty(): Boolean {
        return targetMap.isEmpty()
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, MutableList<V>>>
        get() = this.targetMap.entries
    override val keys: MutableSet<K>
        get() = this.targetMap.keys
    override val values: MutableCollection<MutableList<V>>
        get() = this.targetMap.values

    override fun clear() {
        this.targetMap.clear()
    }

    override fun put(key: K, value: MutableList<V>): MutableList<V>? {
        return this.targetMap.put(key, value)
    }

    override fun putAll(from: Map<out K, MutableList<V>>) {
        return this.targetMap.putAll(from)
    }

    override fun remove(key: K): MutableList<V>? {
        return this.targetMap.remove(key)
    }

    override fun toSingleValueMap(): Map<K, V> {
        val singletonValueMap = HashMap<K, V>()
        this.targetMap.forEach { (key, values) ->
            if (values.isNotEmpty()) {
                singletonValueMap[key] = values.iterator().next()
            }
        }
        return singletonValueMap
    }

    /**
     * toString
     */
    override fun toString(): String = this.targetMap.toString()
}