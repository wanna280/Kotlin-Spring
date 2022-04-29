package com.wanna.framework.core.environment

/**
 * 这是一个针对于PropertySources的PropertyResolver
 *
 * @see PropertyResolver
 * @see PropertySources
 * @see MutablePropertySources
 */
open class PropertySourcesPropertyResolver(private val propertySources: PropertySources) : AbstractPropertyResolver() {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getProperty(key: String, requiredType: Class<T>): T? {
        propertySources.forEach {
            if (it.containsProperty(key)) {
                val value = it.getProperty(key)
                val conversionService = getConversionService()

                // 如果类型转换能对该类型去进行转换的话，那么...
                if (conversionService.canConvert(value!!::class.java, requiredType)) {
                    return conversionService.convert(value, requiredType)
                }
                return value as T?
            }
        }
        return null
    }

    /**
     * 获取属性的原始值的String，如果找不到的话，直接return key
     */
    override fun getPropertyAsRawString(key: String): String? {
        propertySources.forEach {
            if (it.containsProperty(key)) {
                return it.getProperty(key).toString()
            }
        }
        return null
    }
}