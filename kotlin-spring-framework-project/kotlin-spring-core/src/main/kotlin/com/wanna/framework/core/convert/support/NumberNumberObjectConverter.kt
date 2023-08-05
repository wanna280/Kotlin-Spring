package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter

/**
 * Number->NumberObject和NumberObject->Number的Converter(例如int->Integer, bool->Boolean)
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
class NumberNumberObjectConverter : GenericConverter {

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(
            // 基础->包装
            GenericConverter.ConvertiblePair(Int::class.java, Int::class.javaObjectType),
            GenericConverter.ConvertiblePair(Double::class.java, Double::class.javaObjectType),
            GenericConverter.ConvertiblePair(Float::class.java, Float::class.javaObjectType),
            GenericConverter.ConvertiblePair(Short::class.java, Short::class.javaObjectType),
            GenericConverter.ConvertiblePair(Byte::class.java, Byte::class.javaObjectType),
            GenericConverter.ConvertiblePair(Long::class.java, Long::class.javaObjectType),
            GenericConverter.ConvertiblePair(Boolean::class.java, Boolean::class.javaObjectType),
            GenericConverter.ConvertiblePair(Char::class.java, Char::class.javaObjectType),

            // 包装转基础
            GenericConverter.ConvertiblePair(Int::class.javaObjectType, Int::class.java),
            GenericConverter.ConvertiblePair(Double::class.javaObjectType, Double::class.java),
            GenericConverter.ConvertiblePair(Float::class.javaObjectType, Float::class.java),
            GenericConverter.ConvertiblePair(Short::class.javaObjectType, Short::class.java),
            GenericConverter.ConvertiblePair(Byte::class.javaObjectType, Byte::class.java),
            GenericConverter.ConvertiblePair(Long::class.javaObjectType, Long::class.java),
            GenericConverter.ConvertiblePair(Boolean::class.javaObjectType, Boolean::class.java),
            GenericConverter.ConvertiblePair(Char::class.javaObjectType, Char::class.java),

            // 包装->包装
            GenericConverter.ConvertiblePair(Int::class.javaObjectType, Int::class.javaObjectType),
            GenericConverter.ConvertiblePair(Double::class.javaObjectType, Double::class.javaObjectType),
            GenericConverter.ConvertiblePair(Float::class.javaObjectType, Float::class.javaObjectType),
            GenericConverter.ConvertiblePair(Short::class.javaObjectType, Short::class.javaObjectType),
            GenericConverter.ConvertiblePair(Byte::class.javaObjectType, Byte::class.javaObjectType),
            GenericConverter.ConvertiblePair(Long::class.javaObjectType, Long::class.javaObjectType),
            GenericConverter.ConvertiblePair(Boolean::class.javaObjectType, Boolean::class.javaObjectType),
            GenericConverter.ConvertiblePair(Char::class.javaObjectType, Char::class.javaObjectType)
        )
    }

    override fun <S : Any, T : Any> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        return source as T?
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return source
    }
}