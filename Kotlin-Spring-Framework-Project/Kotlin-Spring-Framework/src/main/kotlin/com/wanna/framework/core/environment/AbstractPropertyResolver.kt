package com.wanna.framework.core.environment

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.core.util.PropertyPlaceholderHelper

/**
 * 这是一个抽象的值解析器，对于大多数的公共部分的方式，通过调用子类必须实现的模板方法去进行实现
 *
 * @see PropertySourcesPropertyResolver
 * @see MutablePropertySources
 * @see PropertySources
 * @see PropertyResolver
 */
abstract class AbstractPropertyResolver : ConfigurablePropertyResolver {

    // 占位符的前缀(本来应该使用${，但是Kotlin当中有自己的${，这里改用%{去进行替代)
    private var placeholderPredix = "%{"

    // 占位符的后缀
    private var placeholderSuffix = "}"

    // 值分割器，分割默认值
    private var valueSeparator: String? = ":"

    private var conversionService: ConversionService? = null

    // 占位符解析的工具类
    private val placeholderHelper = PropertyPlaceholderHelper(placeholderPredix, placeholderSuffix, valueSeparator)

    override fun getConversionService(): ConversionService {
        var conversionService = this.conversionService
        if (conversionService == null) {
            synchronized(this) {
                conversionService = this.conversionService
                if (conversionService == null) {
                    conversionService = DefaultConversionService()
                    this.conversionService = conversionService
                }
            }
        }
        return conversionService!!
    }

    override fun setConversionService(conversionService: ConversionService) {
        this.conversionService = conversionService
    }

    override fun setPlaceholderPrefix(prefix: String) {
        this.placeholderPredix = prefix
    }

    override fun setPlaceholderSuffix(suffix: String) {
        this.placeholderSuffix = suffix
    }

    override fun setValueSeparator(separator: String?) {
        this.valueSeparator = separator
    }

    override fun containsProperty(key: String): Boolean = getProperty(key) != null

    override fun getProperty(key: String): String? = getProperty(key, String::class.java)

    override fun getProperty(key: String, defaultValue: String?): String? = getProperty(key) ?: defaultValue

    override fun <T> getProperty(key: String, requiredType: Class<T>, defaultValue: T): T =
        getProperty(key, requiredType) ?: defaultValue

    override fun getRequiredProperty(key: String): String =
        getProperty(key) ?: throw IllegalStateException("无法找到属性值[key=$key]，最终解析到的结果为null")

    override fun <T> getRequiredProperty(key: String, requiredType: Class<T>): T =
        getProperty(key, requiredType) ?: throw IllegalStateException("无法找到属性值[key=$key]，最终解析到的结果为null")

    override fun resolveRequiredPlaceholders(text: String): String =
        resolvePlaceholders(text) ?: throw IllegalStateException("无法找到解析占位符text=$text]，最终解析到的结果为null")

    override fun resolvePlaceholders(text: String): String? {
        return placeholderHelper.replacePlaceholder(text, this::getPropertyAsRawString)
    }

    /**
     * 将属性值解析为原始的字符串，需要子类去进行实现
     */
    abstract fun getPropertyAsRawString(key: String): String?
}