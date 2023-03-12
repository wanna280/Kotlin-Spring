package com.wanna.middleware.cli

import com.wanna.middleware.cli.converter.Converter

/**
 * 支持去将Argument的字符串参数值, 去转换成为目标类型的Argument
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 *
 * @param T 需要将字符串去转换成为的目标类型
 */
open class TypedArgument<T> : Argument() {

    /**
     * 需要将字符串去转换成为的目标类型
     */
    private var type: Class<T>? = null

    /**
     * 用于将字符串去转换成为目标类型的Converter
     */
    private var converter: Converter<T>? = null

    open fun getType(): Class<T> {
        return this.type ?: throw IllegalStateException("type has not been initialized")
    }

    open fun setType(type: Class<T>): TypedArgument<T> {
        this.type = type
        return this
    }

    open fun getConverter(): Converter<T>? {
        return this.converter
    }

    open fun setConverter(converter: Converter<T>): TypedArgument<T> {
        this.converter = converter
        return this
    }

    override fun setDefaultValue(defaultValue: String): TypedArgument<T> {
        super.setDefaultValue(defaultValue)
        return this
    }

}