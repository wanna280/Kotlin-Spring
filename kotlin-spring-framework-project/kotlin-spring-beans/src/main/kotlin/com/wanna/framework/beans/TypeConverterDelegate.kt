package com.wanna.framework.beans

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils

/**
 * TypeConverter的委托工具类
 */
class TypeConverterDelegate(private val registry: PropertyEditorRegistrySupport) {

    /**
     * 如果必要的话, 需要去完成类型的转换
     *
     * @param propertyName propertyName(可以为null)
     * @param oldValue 该属性的旧的值(可以为null)
     * @param newValue 该属性的新值(不能为null)
     * @param requiredType 需要转换成为的类型(不能为null)
     * @return 转换之后的属性值
     * @throws IllegalArgumentException 参数类型不匹配的话
     */
    @Nullable
    @Throws(IllegalArgumentException::class)
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> convertIfNecessary(
        @Nullable propertyName: String?,
        @Nullable oldValue: Any?,
        @Nullable newValue: Any?,
        requiredType: Class<T>
    ): T? {
        val conversionService = registry.getConversionService()
        // 寻找自定义的PropertyEditor去进行转换...
        // 使用ConversionService去进行转换...

        if (conversionService != null && newValue != null) {
            if (conversionService.canConvert(newValue::class.java, requiredType)) {
                return conversionService.convert(newValue, requiredType)
            }
        }
        var convertedValue: Any? = newValue
        // 使用Editor去进行转换...
        val defaultEditors = registry.defaultEditors
        if (defaultEditors != null && newValue is String) {
            val editor = defaultEditors[requiredType]
            if (editor != null) {
                editor.asText = newValue
                convertedValue = editor.value
            }
        }
        // bugfix...这里如果直接检查isAssignFrom的话, 有可能会存在需要的最终结果当中,
        // 其中一个是基础类型、一个是包装类型, 从而导致问题, 因此我们还是使用cast去进行检查吧
        try {
            return convertedValue as T
        } catch (ex: Exception) {
            // 如果参数类型转换存在问题的话, 那么丢出去不合法参数异常...
            throw IllegalArgumentException(
                "参数类型不匹配, 需要的类型是[${ClassUtils.getQualifiedName(requiredType)}], 但是实际得到的是[${
                    ClassUtils.getQualifiedName(convertedValue?.javaClass ?: Unit::class.java)
                }]"
            )
        }
    }
}