package com.wanna.logger.impl.utils

import java.util.ServiceLoader

/**
 * ServiceLoader Util
 */
object ServiceLoaderUtils {
    /**
     * 给定具体类型，使用SPI机制加载到第一个元素
     *
     * @param clazz 指定的类型
     * @return 加载到的第一个元素(没有加载到return null)
     */
    @JvmStatic
    fun <T> loadFirst(clazz: Class<T>): T? {
        val all = loadAll(clazz)
        return if (all.isEmpty()) null else all.iterator().next()
    }

    /**
     * 给定具体类型，使用SPI机制加载到全部元素
     *
     * @param clazz 指定的类型
     * @return 加载到全部元素
     */
    @JvmStatic
    fun <T> loadAll(clazz: Class<T>): Collection<T> {
        val loader = ServiceLoader.load(clazz)
        return loader.toList()
    }
}