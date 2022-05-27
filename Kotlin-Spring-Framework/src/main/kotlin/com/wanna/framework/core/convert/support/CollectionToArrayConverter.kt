package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.converter.GenericConverter

/**
 * 将Collection转为Array的Converter
 */
open class CollectionToArrayConverter(private val conversionService: ConversionService? = null) : GenericConverter {
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair>? {
        return setOf(GenericConverter.ConvertiblePair(Collection::class.java, Array<Any>::class.java))
    }

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
}