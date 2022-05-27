package com.wanna.framework.beans

/**
 * 提供TypeConverter的默认实现
 */
open class TypeConverterSupport : PropertyEditorRegistrySupport(), TypeConverter {

    // TypeConverter的委托工具类，同时组合ConversionService和PropertyEditor去进行类型的转换
    protected lateinit var delegate: TypeConverterDelegate

    override fun <T> convertIfNecessary(value: Any?, requiredType: Class<T>?): T? {
        if (value == null) {
            return null
        }
        if (requiredType == null) {
            return null
        }
        return delegate.convertIfNecessary(null, null, value, requiredType)
    }
}