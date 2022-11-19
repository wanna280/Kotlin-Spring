package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.ConvertiblePair

@Suppress("UNCHECKED_CAST")
open class StringToStringConverter : GenericConverter {
    override fun getConvertibleTypes() = setOf(ConvertiblePair(String::class.java, String::class.java))

    override fun <S : Any, T : Any> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>) = source as T?

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor) = source

    override fun toString() = getConvertibleTypes().toString()
}