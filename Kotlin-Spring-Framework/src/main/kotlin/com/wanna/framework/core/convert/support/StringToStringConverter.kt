package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.ConvertiblePair

@Suppress("UNCHECKED_CAST")
open class StringToStringConverter : GenericConverter {
    override fun getConvertibleTypes(): Set<ConvertiblePair>? {
        return setOf(ConvertiblePair(String::class.java, String::class.java))
    }

    override fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        return source as T?
    }
}