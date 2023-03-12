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

    init {
        // 初始化基础类型->包装类型的映射关系缓存
        PRIMITIVE_TO_WRAPPER_TYPE[Char::class.java] = Char::class.javaObjectType
        PRIMITIVE_TO_WRAPPER_TYPE[Byte::class.java] = Byte::class.javaObjectType
        PRIMITIVE_TO_WRAPPER_TYPE[Boolean::class.java] = Boolean::class.javaObjectType
        PRIMITIVE_TO_WRAPPER_TYPE[Short::class.java] = Long::class.javaObjectType
        PRIMITIVE_TO_WRAPPER_TYPE[Int::class.java] = Int::class.javaObjectType
        PRIMITIVE_TO_WRAPPER_TYPE[Long::class.java] = Long::class.javaObjectType
        PRIMITIVE_TO_WRAPPER_TYPE[Float::class.java] = Float::class.javaObjectType
        PRIMITIVE_TO_WRAPPER_TYPE[Double::class.java] = Double::class.javaObjectType
    }

    /**
     * 使用给定的Converter, 去将给定的字符串去转换成为目标对象
     *
     * @param value 待进行转换的字符串
     * @param converter 用于进行字符串的转换的Converter
     * @return 转换成为的目标类型的对象
     *
     * @param T 目标对象类型
     */
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

    /**
     * 为给定的类型, 去获取到转换成为该类型的目标对象的Converter
     *
     * @param type 要去进行转换的目标类型
     * @return 用于转换成为目标类型的对象的Converter
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    private fun <T> getConverter(type: Class<T>): Converter<T> {

        // 如果是String, 那么直接使用StringConverter
        if (type == String::class.java) {
            return StringConverter as Converter<T>
        }

        // 如果是Boolean, 那么直接使用BooleanConverter
        if (type == Boolean::class.java) {
            return BooleanConverter as Converter<T>
        }

        // 如果还是无法尝试的话, 那么继续尝试基于目标类型的类的构造器去进行构建Converter
        var converter: Converter<T>? = ConstructorBasedConverter.getIfEligible(type)
        if (converter != null) {
            return converter
        }

        // 如果基于构造器也尝试失败的话, 那么继续尝试基于类的valueOf方法去构建Converter
        converter = ValueOfBasedConverter.getIfEligible(type)
        if (converter != null) {
            return converter
        }
        throw NoSuchElementException()
    }

    /**
     * 获取到给定类型的包装类, 比如int.class, 可以获取到Integer.class
     *
     * @param type type
     * @return 给定的type类型对应的包装类型, 如果type不是基础类型, 那么return type
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    private fun <T> wrap(type: Class<T>): Class<T> {
        val wrapper = PRIMITIVE_TO_WRAPPER_TYPE[type]
        return (wrapper ?: type) as Class<T>
    }

}