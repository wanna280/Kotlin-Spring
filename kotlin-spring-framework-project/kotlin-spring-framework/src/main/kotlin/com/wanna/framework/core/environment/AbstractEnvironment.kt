package com.wanna.framework.core.environment

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.lang.Nullable

/**
 * 这是一个环境的抽象实现, 提供了一个标准的环境相关的属性的解析的方法实现
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
 *
 * @param propertyResolver 提供属性的解析的PropertyResolver
 * @param propertySources 提供睡醒的来源PropertySources
 */
abstract class AbstractEnvironment(
    private val propertySources: MutablePropertySources = MutablePropertySources(),
    private val propertyResolver: PropertySourcesPropertyResolver = PropertySourcesPropertyResolver(propertySources)
) : ConfigurableEnvironment {

    companion object {
        /**
         * 需要去配置激活的profiles的属性名
         */
        const val ACTIVE_PROFILES_PROPERTY_NAME = "spring.profiles.active"

        /**
         * 配置默认的profiles的属性名
         */
        const val DEFAULT_PROFILES_PROPERTY_NAME = "spring.profiles.default"
    }


    /**
     * 提供一个基于PropertySources的构建方式
     *
     * @param propertySources PropertySources
     */
    constructor(propertySources: MutablePropertySources) : this() {
        propertySources.forEach { this.propertySources.addLast(it) }
    }

    /**
     * 要去进行激活的profiles
     */
    private var activeProfiles: MutableSet<String> = LinkedHashSet()

    /**
     * 默认的profiles
     */
    private var defaultProfiles: MutableSet<String> = LinkedHashSet(setOf("default"))

    init {
        // 在初始化Environment时, 自动去执行customizePropertySources, 完成PropertySources当中的PropertySource列表的初始化工作
        this.customizePropertySources(propertySources)
    }

    /**
     * 子类有可能需要根据MutablePropertySources去对其进行自定义, 需要提供这么一个扩展接口
     *
     * @param propertySources 待进行自定义的PropertySources
     */
    protected open fun customizePropertySources(propertySources: MutablePropertySources) {}

    /**
     * 设置ActiveProfiles
     *
     * @param profiles active profiles
     */
    override fun setActiveProfiles(vararg profiles: String) {
        this.activeProfiles = LinkedHashSet(profiles.toList())
    }

    /**
     * 添加ActiveProfiles
     *
     * @param profile active profiles
     */
    override fun addActiveProfiles(profile: String) {
        this.activeProfiles += profile
    }

    /**
     * 获取默认的Profiles
     *
     * @return default profiles
     */
    override fun getDefaultProfiles(): Array<String> = defaultProfiles.toTypedArray()

    /**
     * 获取要去进行激活的Profiles
     *
     * @return active profiles
     */
    override fun getActiveProfiles(): Array<String> = activeProfiles.toTypedArray()

    /**
     * 设置默认的Profiles
     *
     * @param profiles default profiles
     */
    override fun setDefaultProfiles(vararg profiles: String) {
        this.defaultProfiles = LinkedHashSet(profiles.toList())
    }

    override fun getPropertySources(): MutablePropertySources = this.propertySources

    @Suppress("UNCHECKED_CAST")
    override fun getSystemProperties(): Map<String, Any> = System.getProperties() as Map<String, Any>

    override fun getSystemEnvironment(): Map<String, Any> = System.getenv()

    override fun setPlaceholderPrefix(prefix: String) = propertyResolver.setPlaceholderPrefix(prefix)

    override fun setPlaceholderSuffix(suffix: String) = propertyResolver.setPlaceholderSuffix(suffix)

    override fun setValueSeparator(@Nullable separator: String?) = propertyResolver.setValueSeparator(separator)

    override fun containsProperty(key: String): Boolean = propertyResolver.containsProperty(key)

    @Nullable
    override fun getProperty(key: String): String? = propertyResolver.getProperty(key)

    override fun getProperty(key: String, defaultValue: String): String =
        propertyResolver.getProperty(key, defaultValue)

    @Nullable
    override fun <T : Any> getProperty(key: String, requiredType: Class<T>): T? =
        propertyResolver.getProperty(key, requiredType)

    override fun <T : Any> getProperty(key: String, requiredType: Class<T>, defaultValue: T): T =
        propertyResolver.getProperty(key, requiredType, defaultValue)

    override fun getRequiredProperty(key: String): String = propertyResolver.getRequiredProperty(key)

    override fun <T : Any> getRequiredProperty(key: String, requiredType: Class<T>): T =
        propertyResolver.getRequiredProperty(key, requiredType)

    @Nullable
    override fun resolvePlaceholders(text: String): String? = propertyResolver.resolvePlaceholders(text)

    override fun resolveRequiredPlaceholders(text: String): String = propertyResolver.resolveRequiredPlaceholders(text)

    override fun getConversionService(): ConversionService = this.propertyResolver.getConversionService()

    override fun setConversionService(conversionService: ConversionService) =
        this.propertyResolver.setConversionService(conversionService)
}