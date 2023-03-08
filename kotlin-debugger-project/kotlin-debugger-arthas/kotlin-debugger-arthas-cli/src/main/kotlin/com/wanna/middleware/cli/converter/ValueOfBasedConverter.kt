package com.wanna.middleware.cli.converter

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import javax.annotation.Nullable

/**
 * 基于类当中的valueOf的静态方法去进行转换的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/3/6
 */
open class ValueOfBasedConverter<T>(private val clazz: Class<T>, private val method: Method) : Converter<T> {

    @Nullable
    override fun fromString(@Nullable string: String?): T? {
        try {
            return clazz.cast(method.invoke(null, string))
        } catch (ex: Throwable) {
            // 如果ex.cause不为null的话, 那么把cause包装丢出去; 如果ex.cause为null的话, 那么把ex包装丢出去
            throw IllegalArgumentException(ex.cause ?: ex)
        }
    }

    companion object {

        /**
         * 基于给定的类的valueOf的静态方法, 去包装成为ValueOfBasedConverter
         *
         * @param clazz clazz
         * @return ValueOfBasedConverter(or null)
         */
        @Nullable
        @JvmStatic
        fun <T> getIfEligible(clazz: Class<T>): ValueOfBasedConverter<T>? {
            val method = clazz.getMethod("valueOf", String::class.java)
            if (Modifier.isStatic(method.modifiers)) {
                if (!method.isAccessible) {
                    method.isAccessible = true
                }
                return ValueOfBasedConverter(clazz, method)
            }
            return null
        }
    }
}