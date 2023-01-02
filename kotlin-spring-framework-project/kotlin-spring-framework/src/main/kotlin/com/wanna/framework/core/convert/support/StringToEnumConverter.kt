package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter

/**
 * String转Enum的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/2
 */
open class StringToEnumConverter : GenericConverter {

    /**
     * String->Enum
     *
     * @return Pair of String->Enum
     */
    override fun getConvertibleTypes() = setOf(GenericConverter.ConvertiblePair(String::class.java, Enum::class.java))

    /**
     * 将字符串转换为枚举值
     *
     * @param source source
     * @param sourceType sourceType
     * @param targetType targetType
     * @return 转换之后得到的枚举值
     */
    @Suppress("UNCHECKED_CAST")
    override fun <S : Any, T : Any> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        source ?: return null
        if (source is String && targetType.isEnum) {
            return java.lang.Enum.valueOf(targetType as Class<out Enum<*>>, source) as T?
        }
        return null
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return convert(source, sourceType.type, targetType.type)
    }
}