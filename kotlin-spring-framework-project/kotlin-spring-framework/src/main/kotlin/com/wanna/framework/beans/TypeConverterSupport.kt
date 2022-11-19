package com.wanna.framework.beans

/**
 * 提供TypeConverter的默认实现
 */
open class TypeConverterSupport : PropertyEditorRegistrySupport(), TypeConverter {

    // TypeConverter的委托工具类，同时组合ConversionService和PropertyEditor去进行类型的转换
    protected var delegate: TypeConverterDelegate? = null

    @Throws(TypeMismatchException::class)
    override fun <T : Any> convertIfNecessary(value: Any?, requiredType: Class<T>?): T? {
        value ?: return null
        requiredType ?: return null

        try {
            return delegate?.convertIfNecessary(null, null, value, requiredType)
        } catch (ex: IllegalArgumentException) {
            throw TypeMismatchException(value, requiredType, ex)
        }
    }
}