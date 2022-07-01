package com.wanna.framework.beans

/**
 * TypeConverter的委托工具类
 */
class TypeConverterDelegate(private val registry: PropertyEditorRegistrySupport) {

    /**
     * 如果必要的话，需要去完成类型的转换
     *
     * @param propertyName propertyName(可以为null)
     * @param oldValue 该属性的旧的值(可以为null)
     * @param newValue 该属性的新值(不能为null)
     * @param requiredType 需要转换成为的类型(不能为null)
     * @return 转换之后的属性值
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> convertIfNecessary(propertyName: String?, oldValue: Any?, newValue: Any, requiredType: Class<T>): T? {
        val conversionService = registry.getConversionService()
        // 寻找自定义的PropertyEditor去进行转换...
        // 使用ConversionService去进行转换...

        if (conversionService != null) {
            if (conversionService.canConvert(newValue::class.java, requiredType)) {
                return conversionService.convert(newValue, requiredType)
            }
        }
        var convertedValue: Any = newValue
        // 使用Editor去进行转换...
        val defaultEditors = registry.defaultEditors
        if (defaultEditors != null && newValue is String) {
            val editor = defaultEditors[requiredType]
            if (editor != null) {
                editor.asText = newValue
                convertedValue = editor.value
            }
        }

        // 强制类型转换...
        return convertedValue as T
    }
}