package com.wanna.framework.core.environment

import com.wanna.framework.lang.Nullable

/**
 * 这是一个属性的解析器, 可以通过它去完成属性的解析
 *
 * @see Environment
 * @see ConfigurablePropertyResolver
 */
interface PropertyResolver {

    /**
     * 是否包含有这个属性？
     *
     * @param key 属性名
     * @return 如果包含有这样的属性名return true; 否则return false
     */
    fun containsProperty(key: String): Boolean

    /**
     * 获取属性, 如果获取不到, return null
     */
    @Nullable
    fun getProperty(key: String): String?

    /**
     * 根据key去获取属性, 如果获取不到返回默认值
     *
     * @param key 属性key
     * @param defaultValue 获取不到该属性值时, 需要返回的默认值
     */
    fun getProperty(key: String, defaultValue: String): String

    /**
     * 按照指定的类型去获取属性, 如果获取不到, return null
     *
     * @param key 属性key
     * @param requiredType 属性值需要的目标类型
     */
    @Nullable
    fun <T : Any> getProperty(key: String, requiredType: Class<T>): T?

    /**
     * 按照指定的类型去获取属性, 如果获取不到, return默认值(defaultValue)
     *
     * @param key 属性key
     * @param requiredType 属性值需要的目标类型
     */
    fun <T : Any> getProperty(key: String, requiredType: Class<T>, defaultValue: T): T

    /**
     * 根据key去获取属性, 如果获取不到, 抛出IllegalStateException
     *
     * @param key 需要获取属性的属性key
     */
    fun getRequiredProperty(key: String): String

    /**
     * 根据key去按照指定的类型去获取属性, 如果获取不到, 抛出IllegalStateException
     *
     * @param key 属性key
     * @param requiredType 需要的类型
     */
    fun <T : Any> getRequiredProperty(key: String, requiredType: Class<T>): T

    /**
     * 解析占位符, 如果解析不到, return null
     *
     * @param text 待进行占位符的解析的文本
     */
    fun resolvePlaceholders(text: String): String?

    /**
     * 根据key去解析占位符, 如果解析不到, 抛出IllegalStateException
     *
     * @param text 待进行占位符的解析的文本
     */
    fun resolveRequiredPlaceholders(text: String): String
}