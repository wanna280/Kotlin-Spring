package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.*
import com.wanna.framework.util.StringUtils

/**
 * 将String转为Array的Converter，支持对将String类型的转换类型转换为对应的元素类型
 *
 * @param conversionService ConversionService for convert elementTyp
 */
open class StringToArrayConverter(val conversionService: ConversionService) : GenericConverter {
    override fun getConvertibleTypes() = setOf(ConvertiblePair(String::class.java, Array::class.java))

    @Suppress("UNCHECKED_CAST")
    override fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        if (source is String && targetType.isArray) {
            val elementType = targetType.componentType

            // 将String去转换为StringArray(使用","去进行分割)
            val sourceList = StringUtils.commaDelimitedListToStringArray(source)

            // 基于Java反射包下的Array类，去创建一个数组
            val array = java.lang.reflect.Array.newInstance(elementType, sourceList.size)

            // 遍历sourceList当中的全部元素(String)，挨个交给ConversionService去进行类型转换
            for (index in sourceList.indices) {
                val convertedElement = conversionService.convert(sourceList[index], elementType)
                java.lang.reflect.Array.set(array, index, convertedElement)
            }
            return array as T
        }
        return null
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? =
        convert(source, sourceType.type, targetType.type)

    override fun toString() = getConvertibleTypes().toString()
}