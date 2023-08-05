package com.wanna.boot.origin

import com.wanna.framework.lang.Nullable

/**
 * 实现这个接口的类的对象可以通过一个给定Key去获取到它的[Origin]的相关信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/7
 *
 * @see Origin
 */
fun interface OriginLookup<K> {
    /**
     * 根据key去获取到对应的Origin
     *
     * @param key key
     * @return origin(or null)
     */
    fun getOrigin(key: K): Origin?

    /**
     * 是否是不可变的?
     *
     * @return immutable
     */
    fun isImmutable(): Boolean = false

    fun getPrefix(): String? = null

    companion object {

        /**
         * 如果给定的source是OriginLookup, 那么使用它的getOrigin方法去获取到对应的Origin信息;
         * 如果给定的source不是OriginLookup, 或者getOrigin执行发生异常, 那么return null
         *
         * @param source source
         * @param key key
         * @return origin
         * @return Origin(or null)
         */
        @JvmStatic
        @Nullable
        @Suppress("UNCHECKED_CAST")
        fun <K> getOrigin(source: Any, key: K): Origin? {
            if (source !is OriginLookup<*>) {
                return null
            }
            return try {
                (source as OriginLookup<K>).getOrigin(key)
            } catch (ex: Exception) {
                null
            }
        }
    }
}