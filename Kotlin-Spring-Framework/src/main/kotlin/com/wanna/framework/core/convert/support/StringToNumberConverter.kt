package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.ConvertiblePair

/**
 * 这是一个可以将String转换为Number类型的Converter
 */
@Suppress("UNCHECKED_CAST")
class StringToNumberConverter : GenericConverter {
    private val convertiblePairs = HashSet<ConvertiblePair>()

    init {
        // 初始化它支持转换的映射列表
        convertiblePairs.add(ConvertiblePair(String::class.java, Int::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Byte::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Double::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Float::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Short::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Char::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Long::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Boolean::class.java))
    }

    override fun getConvertibleTypes(): Set<ConvertiblePair> {
        return convertiblePairs
    }

    override fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        if (source == null) {
            return null
        }
        val sourceStr = source.toString()
        if (targetType == Int::class.java) {
            return sourceStr.toInt() as T
        }
        if (targetType == Byte::class.java) {
            return sourceStr.toByte() as T
        }
        if (targetType == Long::class.java) {
            return sourceStr.toLong() as T
        }
        if (targetType == Double::class.java) {
            return sourceStr.toDouble() as T
        }
        if (targetType == Short::class.java) {
            return sourceStr.toShort() as T
        }
        if (targetType == Float::class.java) {
            return sourceStr.toFloat() as T
        }
        if (targetType == Char::class.java) {
            return sourceStr.toInt().toChar() as T
        }
        if (targetType == Boolean::class.java) {
            return sourceStr.toBoolean() as T
        }
        throw UnsupportedOperationException("支持将sourceType=[$sourceType]转换为targetType=[$targetType]")
    }
}