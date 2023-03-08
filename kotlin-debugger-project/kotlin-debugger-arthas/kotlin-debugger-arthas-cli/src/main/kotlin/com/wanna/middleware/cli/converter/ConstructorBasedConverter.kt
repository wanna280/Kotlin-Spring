package com.wanna.middleware.cli.converter

import java.lang.reflect.Constructor
import javax.annotation.Nullable

/**
 * 基于构造器的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/3/6
 */
open class ConstructorBasedConverter<T>(private val constructor: Constructor<T>) : Converter<T> {

    @Nullable
    override fun fromString(@Nullable string: String?): T? {
        try {
            return constructor.newInstance(string)
        } catch (ex: Throwable) {
            // 如果ex.cause不为null的话, 那么把cause包装丢出去; 如果ex.cause为null的话, 那么把ex包装丢出去
            throw IllegalArgumentException(ex.cause ?: ex)
        }
    }

    companion object {

        /**
         * 根据给定的类, 去获取到它的构造器, 并包装成为Converter,
         * 对于转换时, 直接将该参数传递到构造器当中, 去进行包装一层, 从而实现类型的转换.
         *
         * 比如java.lang.Integer/java.lang.Long都有以String作为参数的构造器,
         * 因此都能很完美的去进行类型转换
         *
         * @param clazz clazz
         * @return ConstructorBasedConverter(or null)
         */
        @Nullable
        @JvmStatic
        fun <T> getIfEligible(clazz: Class<T>): ConstructorBasedConverter<T>? {
            try {
                val constructor = clazz.getConstructor(String::class.java)
                if (!constructor.isAccessible) {
                    constructor.isAccessible = true
                }
                return ConstructorBasedConverter(constructor)
            } catch (ex: NoSuchMethodException) {
                return null
            }
        }
    }
}