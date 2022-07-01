package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.*
import com.wanna.framework.core.util.StringUtils

/**
 * Collection转String的Converter
 */
open class CollectionToStringConverter : GenericConverter {
    override fun getConvertibleTypes() = setOf(ConvertiblePair(Collection::class.java, String::class.java))

    @Suppress("UNCHECKED_CAST")
    override fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        source ?: return null
        return StringUtils.collectionToCommaDelimitedString((source as Collection<Any>).map { it.toString() }
            .toList()) as T?
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? =
        convert(source, sourceType.type, targetType.type)

    override fun toString() = getConvertibleTypes().toString()
}