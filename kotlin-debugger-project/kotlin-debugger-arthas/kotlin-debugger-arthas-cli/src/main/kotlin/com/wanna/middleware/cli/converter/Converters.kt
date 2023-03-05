package com.wanna.middleware.cli.converter

import javax.annotation.Nullable

/**
 * 提供对于[Converter]相关的工具方法的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/27
 */
object Converters {

    /**
     * 基础类型->包装类型的映射关系缓存
     */
    @JvmStatic
    private val PRIMITIVE_TO_WRAPPER_TYPE = LinkedHashMap<Class<*>, Class<*>>()

    @Nullable
    @JvmStatic
    fun <T> create(@Nullable value: String?, converter: Converter<T>): T? {
        return converter.fromString(value)
    }

    @Nullable
    @JvmStatic
    fun <T> create(type: Class<T>, @Nullable value: String?): T? {
        var typeToUse = type
        if (typeToUse.isPrimitive) {
            typeToUse = wrap(type)
        }
        return getConverter(typeToUse).fromString(value)
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    private fun <T> getConverter(type: Class<T>): Converter<T> {
        if (type == String::class.java) {
            return StringConverter as Converter<T>
        }
        if (type == Boolean::class.java) {
            return BooleanConverter as Converter<T>
        }
        val converter = ConstructorBasedConverter.getIfEligible(type)
        if (converter != null) {
            return converter
        }
        throw NoSuchElementException()
    }

    /**
     * 获取到给定类型的包装类
     *
     * @param type type
     * @return 给类型的包装类型
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    private fun <T> wrap(type: Class<T>): Class<T> {
        val wrapper = PRIMITIVE_TO_WRAPPER_TYPE[type]
        return (wrapper ?: type) as Class<T>
    }

}