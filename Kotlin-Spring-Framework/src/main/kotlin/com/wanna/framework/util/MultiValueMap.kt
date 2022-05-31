package com.wanna.framework.util

/**
 * 多值Map，一个Key可以拥有多个Value(value存放的是一个List)
 */
interface MultiValueMap<K, V> : MutableMap<K, MutableList<V>> {

    /**
     * 给定一个key，去获取到该key对应的第一个value
     *
     * @param key key
     * @return 寻找到的第一个value(如果不存在的话，return null)
     */
    fun getFirst(key: K): V?

    /**
     * 往多值Map当中的指定key的value列表添加一个Value
     *
     * @param key 要去添加的key
     * @param value 该key要添加的value
     */
    fun add(key: K, value: V)

    /**
     * 往多值Map当中的指定key的value添加多个Value
     *
     * @param key 要去添加的key
     * @param values 该key要添加的value列表
     */
    fun addAll(key: K, values: List<V>)

    /**
     * 设置多值Map当中的给定一个key的具体的值
     *
     * @param key key
     * @param value 该key要去进行设置的值
     */
    fun set(key: K, value: V)

    /**
     * 转换为一个普通的Map，如果每个key有多个value的话，那么只取其中一个
     *
     * @return 转换之后的单个值的Map
     */
    fun toSingleValueMap(): Map<K, V>
}