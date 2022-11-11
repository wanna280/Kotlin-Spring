package com.wanna.framework.core.environment

/**
 * 标识这是一个属性的解析器，可以通过它去完成属性的解析
 *
 * @see Environment
 * @see ConfigurablePropertyResolver
 */
interface PropertyResolver {

    /**
     * 是否包含有这个属性？
     */
    fun containsProperty(key: String): Boolean

    /**
     * 获取属性，如果获取不到，return null
     */
    fun getProperty(key: String): String?

    /**
     * 根据key去获取属性，如果获取不到返回默认值
     */
    fun getProperty(key: String, defaultValue: String?): String?

    /**
     * 按照指定的类型去获取属性，如果获取不到，return null
     */
    fun <T> getProperty(key: String, requiredType: Class<T>): T?

    /**
     * 按照指定的类型去获取属性，如果获取不到，return默认值(defaultValue)
     */
    fun <T> getProperty(key: String, requiredType: Class<T>, defaultValue: T): T

    /**
     * 根据key去获取属性，如果获取不到，抛出IllegalStateException
     */
    fun getRequiredProperty(key: String): String

    /**
     * 根据key去按照指定的类型去获取属性，如果获取不到，抛出IllegalStateException
     */
    fun <T> getRequiredProperty(key: String, requiredType: Class<T>): T

    /**
     * 解析占位符，如果解析不到，return null
     */
    fun resolvePlaceholders(text: String): String?

    /**
     * 根据key去解析占位符，如果解析不到，抛出IllegalStateException
     */
    fun resolveRequiredPlaceholders(text: String): String
}