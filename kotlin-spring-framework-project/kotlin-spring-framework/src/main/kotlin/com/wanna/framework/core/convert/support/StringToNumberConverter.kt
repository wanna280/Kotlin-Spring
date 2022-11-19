package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.ConvertiblePair

/**
 * 这是一个可以将String转换为Number类型的Converter，支持转为基础类型，也支持转换成为包装类型
 *
 * @see GenericConverter
 */
@Suppress("UNCHECKED_CAST")
open class StringToNumberConverter : GenericConverter {
    private val convertiblePairs = HashSet<ConvertiblePair>()

    // 初始化它支持转换的映射列表
    init {
        // 1.初始化它转为基础数据类型的映射列表
        convertiblePairs.add(ConvertiblePair(String::class.java, Int::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Byte::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Double::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Float::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Short::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Char::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Long::class.java))
        convertiblePairs.add(ConvertiblePair(String::class.java, Boolean::class.java))

        // 2.(fixed:)初始化它转换为包装类型的映射列表(Note: XXX::class.javaObjectType，可以获取到包装类型...)
        convertiblePairs.add(ConvertiblePair(String::class.java, Int::class.javaObjectType))
        convertiblePairs.add(ConvertiblePair(String::class.java, Byte::class.javaObjectType))
        convertiblePairs.add(ConvertiblePair(String::class.java, Double::class.javaObjectType))
        convertiblePairs.add(ConvertiblePair(String::class.java, Float::class.javaObjectType))
        convertiblePairs.add(ConvertiblePair(String::class.java, Short::class.javaObjectType))
        convertiblePairs.add(ConvertiblePair(String::class.java, Char::class.javaObjectType))
        convertiblePairs.add(ConvertiblePair(String::class.java, Long::class.javaObjectType))
        convertiblePairs.add(ConvertiblePair(String::class.java, Boolean::class.javaObjectType))
    }

    override fun getConvertibleTypes() = this.convertiblePairs

    override fun <S : Any, T : Any> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        val sourceStr = source?.toString() ?: return null

        if (targetType == Int::class.java || targetType == Int::class.javaObjectType) {
            return sourceStr.toInt() as T?
        }
        if (targetType == Byte::class.java || targetType == Byte::class.javaObjectType) {
            return sourceStr.toByte() as T?
        }
        if (targetType == Long::class.java || targetType == Long::class.javaObjectType) {
            return sourceStr.toLong() as T?
        }
        if (targetType == Double::class.java || targetType == Double::class.javaObjectType) {
            return sourceStr.toDouble() as T?
        }
        if (targetType == Short::class.java || targetType == Short::class.javaObjectType) {
            return sourceStr.toShort() as T?
        }
        if (targetType == Float::class.java || targetType == Float::class.javaObjectType) {
            return sourceStr.toFloat() as T?
        }
        if (targetType == Char::class.java || targetType == Char::class.javaObjectType) {
            return sourceStr.toInt().toChar() as T?
        }
        if (targetType == Boolean::class.java || targetType == Boolean::class.javaObjectType) {
            return sourceStr.toBoolean() as T?
        }
        throw UnsupportedOperationException("不支持将sourceType=[$sourceType]转换为targetType=[$targetType]")
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? =
        convert(source, sourceType.type, targetType.type)

    override fun toString() = getConvertibleTypes().toString()
}