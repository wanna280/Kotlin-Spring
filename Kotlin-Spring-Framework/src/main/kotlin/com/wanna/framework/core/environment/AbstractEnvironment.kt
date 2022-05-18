package com.wanna.framework.core.environment

import com.wanna.framework.core.convert.ConversionService

/**
 * 这是一个环境的抽象实现，提供了一个标准的环境的实现
 *
 * @see ConfigurablePropertyResolver
 * @see PropertySourcesPropertyResolver
 * @see Environment
 * @see ConfigurableEnvironment
 * @see StandardEnvironment
 * @see PropertySources
 * @see PropertySource
 * @see MutablePropertySources
 * @see ConversionService
 */
abstract class AbstractEnvironment() : ConfigurableEnvironment {

    constructor(propertySources: MutablePropertySources) : this() {
        propertySources.forEach { this.propertySources.addLast(it) }
    }

    private val propertySources = MutablePropertySources()

    // 创建PropertySources的PropertyResolver，去完成属性值的解析
    private val propertyResolver = PropertySourcesPropertyResolver(propertySources)

    // 活跃的profiles
    private var activeProfiles = HashSet<String>()

    // 默认的profiles
    private var defaultProfiles = HashSet<String>()

    init {
        // 在初始化时，自动去执行customizePropertySources，完成PropertySources当中的PropertySource列表的初始化工作
        this.customizePropertySources(propertySources)
    }

    /**
     * 子类有可能需要根据MutablePropertySources去对其进行自定义，需要提供这么一个扩展接口
     */
    protected open fun customizePropertySources(propertySources: MutablePropertySources) {

    }

    override fun setActiveProfiles(vararg profiles: String) {
        this.activeProfiles = HashSet(profiles.toList())
    }

    override fun addActiveProfiles(profile: String) {
        this.activeProfiles += profile
    }

    override fun getDefaultProfiles(): Array<String> {
        return defaultProfiles.toTypedArray()
    }

    override fun getActiveProfiles(): Array<String> {
        return activeProfiles.toTypedArray()
    }

    override fun setDefaultProfiles(vararg profiles: String) {
        this.defaultProfiles = HashSet(profiles.toList())
    }

    override fun getPropertySources(): MutablePropertySources {
        return propertySources
    }

    @Suppress("UNCHECKED_CAST")
    override fun getSystemProperties(): Map<String, Any> {
        return System.getProperties() as Map<String, Any>
    }

    override fun getSystemEnvironment(): Map<String, Any> {
        return System.getenv()
    }

    override fun setPlaceholderPrefix(prefix: String) {
        propertyResolver.setPlaceholderPrefix(prefix)
    }

    override fun setPlaceholderSuffix(suffix: String) {
        propertyResolver.setPlaceholderSuffix(suffix)
    }

    override fun setValueSeparator(separator: String?) {
        propertyResolver.setValueSeparator(separator)
    }


    override fun containsProperty(key: String): Boolean {
        return propertyResolver.containsProperty(key)
    }

    override fun getProperty(key: String): String? {
        return propertyResolver.getProperty(key)
    }

    override fun getProperty(key: String, defaultValue: String?): String? {
        return propertyResolver.getProperty(key, defaultValue)
    }

    override fun <T> getProperty(key: String, requiredType: Class<T>): T? {
        return propertyResolver.getProperty(key, requiredType)
    }

    override fun <T> getProperty(key: String, requiredType: Class<T>, defaultValue: T): T {
        return propertyResolver.getProperty(key, requiredType, defaultValue)
    }

    override fun getRequiredProperty(key: String): String {
        return propertyResolver.getRequiredProperty(key)
    }

    override fun <T> getRequiredProperty(key: String, requiredType: Class<T>): T {
        return propertyResolver.getRequiredProperty(key, requiredType)
    }

    override fun resolvePlaceholders(text: String): String? {
        return propertyResolver.resolvePlaceholders(text)
    }

    override fun resolveRequiredPlaceholders(text: String): String {
        return propertyResolver.resolveRequiredPlaceholders(text)
    }

    override fun getConversionService(): ConversionService {
        return this.propertyResolver.getConversionService()
    }

    override fun setConversionService(conversionService: ConversionService) {
        this.propertyResolver.setConversionService(conversionService)
    }
}