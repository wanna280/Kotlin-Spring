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
open class ConstructorBasedConverter<T>(constructor: Constructor<T>) : Converter<T> {

    override fun fromString(string: String?): T? {
        TODO("Not yet implemented")
    }

    companion object {

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