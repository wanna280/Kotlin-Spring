package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.*

/**
 * 将Collection转为Array的Converter
 */
open class CollectionToArrayConverter(private val conversionService: ConversionService) : GenericConverter {
    override fun getConvertibleTypes() = setOf(ConvertiblePair(Collection::class.java, Array::class.java))

    @Suppress("UNCHECKED_CAST")
    override fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        if (source is Collection<Any?> && targetType.isArray) {
            val array = java.lang.reflect.Array.newInstance(targetType.componentType, source.size)
            val iterator = source.iterator()
            for (index in 0 until source.size) {
                java.lang.reflect.Array.set(array, index, iterator.next())
            }
            return array as T
        }
        return null
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? =
        convert(source, sourceType.type, targetType.type)

    override fun toString() = getConvertibleTypes().toString()
}